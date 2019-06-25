import re

import requests
import logging
from contextlib import closing
from requests import Timeout

import os.path
import sys
sys.path.insert(0, os.path.abspath('.'))
from webmining.charset_detector import detect_charset, encode_text

__author__ = "glebourgeois@me.com"


class FetchResult:
    def __init__(self, fetched_url, webpage, headers, status, content, cookies):
        """
        param: fetched_url: url finally fetched, after eventual redirections
        param: webpage: text content of the fetched URL, might be None (when binary objects as PDF, ...)
        param: headers: http headers in a dictionary
        param: status: http status
        param: content: binary content (always filled)
        """
        self.fetched_url = fetched_url
        self.webpage = webpage
        self.headers = headers
        self.http_status = status
        self.content = content
        self.cookies = cookies

        # Managing content-type
        keys = headers.keys()
        if "content-type" in keys:
            els = headers["content-type"].split(';')
            self.content_type = els[0].strip()
            if len(els) > 1:
                self.charset = els[1].replace("charset=", "").strip()
            else:
                self.charset = None
        else:
            self.charset = None
            self.content_type = None

        self.attachment = None
        if "content-disposition" in keys:
            m = re.search("attachment\s*;\s*filename=[\"']([^\"]*)[\"']", headers["content-disposition"].strip())
            if m is not None:
                self.attachment = m.group(1)


class Fetcher:
    """
    This module can be used to fetch web pages.
    It's able to handle a proxy, and uses a Firefox User Agent.
    """

    def __init__(self, proxy=None, user_agent=None, max_page_size=None):
        """
        :param proxy: Give a proxy object to use to fetch data
        """
        self.proxy = proxy
        self.current_content_type = None
        self.notfound = 0
        self.ok = 0
        self.max_page_size = max_page_size
        self.session = requests.Session()

        if user_agent is None:
            # Windows-8 OS, on a browser Firefox with version 31
            self.headers = {"User-Agent": "Mozilla/5.0 (Windows NT 6.2; rv:31.0) Gecko/20100101 Firefox/31.0"}
        else:
            self.headers = {"User-Agent": user_agent}

        # Logging initialization
        self.logger = logging.getLogger("webmining:fetcher")
        self.logger.setLevel(logging.INFO)

    def fetch(self, url, debug=False, data=None, auth=None, cookies={}, force_encoding=None, timeout=15):
        """
        Fetches a web page (GET method).

        :param auth: authentification tuple (user, password) for basic HTTP authentification
        :param force_encoding: you can provide the encoding of the source you wanna fetch if you know it
                               to avoid costs of encoding detection
        """
        fetched_url, headers, status_code, content, cookies = None, None, None, None, None
        server_encoding = None
        try:
            # Building opener
            proxy = None
            if self.proxy is not None:
                purl = "http://" + self.proxy.username + ":" + self.proxy.password + "@" + self.proxy.url.replace("http://", "")
                proxy = {"http": purl, "https": purl}

            content = None
            # http://docs.python-requests.org/en/latest/user/advanced/#body-content-workflow
            # To be sure we release the connection even when not consuming all data
            with closing(self.session.get(url, timeout=timeout, headers=self.headers, proxies=proxy,
                                          verify=False, auth=auth, cookies=cookies, stream=True)) as r:

                # Save the values for later use
                fetched_url, headers = r.url, r.headers
                status_code, cookies = r.status_code, r.cookies
                server_encoding = r.encoding

                content_length = None
                try:
                    content_length = int(r.headers['content-length'])
                except:
                    pass

                if ((content_length is None or
                   self.max_page_size is None or
                   content_length <= self.max_page_size)):
                    # To be sure that we'll have at least self.max_page_size byte,
                    # Add 1024 bytes more.
                    if self.max_page_size is None:
                        content = r.content
                    else:
                        content = next(r.iter_content(self.max_page_size + 1024))
                    # Now, if we are larger than max_page_size, we probably still
                    # have data to download.
                    if ((self.max_page_size is not None and
                       len(content) > self.max_page_size)):
                        self.logger.warning("Page too long [%s], content's size is %f" % (url, float(len(content))))
                        content = None
        except Timeout as t:
            raise t
        except Exception as e:
            self.logger.warning("Couldn't fetch %s" % url)
            self.logger.warning(e)
            self.notfound += 1
            return None

        text = None
        if content is not None:
            # We resolve to auto-detect encoding
            if force_encoding is None:
                default = server_encoding if server_encoding is not None else "windows-1252"
                encoding = detect_charset(content, default=default)
            else:
                encoding = force_encoding

            text = encode_text(content, encoding)

        fr = FetchResult(fetched_url=fetched_url, webpage=text, headers=headers,
                         status=status_code, content=content, cookies=cookies)

        return fr


class Proxy:
    def __init__(self, url, username, password):
        self.url = url
        self.username = username
        self.password = password


if __name__ == "__main__":
    fetcher = Fetcher()
    res = fetcher.fetch(url="http://www.dudouet.fr", debug=True, timeout=5)
    print(res.webpage)
