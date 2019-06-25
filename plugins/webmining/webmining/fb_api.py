import os
import sys
import json
import re
import logging
import random
import time

sys.path.insert(0, os.path.abspath('.'))
from webmining.fetcher import Fetcher
from webmining.social_apis import RateLimitExceeded


FB_API_URL = "https://graph.facebook.com/v2.4/"


def get_facebook_account(url):
    # First lowercase the url
    url = url.lower()

    # First we select the right http://... part
    m = re.search("https?://(?:.+?\.)?facebook.com(/.+)", url)
    if not m:
        return None
    path = m.group(1)

    # Replace multiple / by only one
    path = re.sub("/+", "/", path)

    # Remove first / if there is still one
    # path = re.sub("^/*", "", path)

    # Ajax redirections
    m = re.search("#!?(/.*)", path)
    if m:
        path = m.group(1)

    # Remove #fragments
    path = re.sub("#.*$", "", path)

    # Remove ?query
    path = re.sub("\\?.*$", "", path)

    # remove & (wrong url end)
    path = re.sub("(&|;).*$", "", path)

    # remove final spaces
    path = path.strip()

    # Blacklist
    BLACKLIST = [
        # All utility pages from facebook
        "/recover", "recover/initiate", "/dialog/", "oauth?", ".php", "/hashtag/", "/share",
        # Medias often points to timeline of people, not companies
        "/photos", "/media/", "/video/", "/media_set", "/notes/",
        # Other things to blacklist
        "/edit/", "/public/", "app_", "/events/"
    ]
    for blacklisted in BLACKLIST:
        if blacklisted in path:
            return None

    m = re.match("^/pages/(?:.*/)*(\d+)/?$", path)
    if m:
        return m.group(1)

    m = re.match("^/people/(?:.*/)*(\d+)$", path)
    if m:
        return m.group(1)

    m = re.match("^/(groups/[^/]+)", path)
    if m:
        return m.group(1)

    m = re.match("^/([^/]+)", path)
    if m:
        account = m.group(1)
        if "http:" in account or "https:" in account:
            return None
        return account

    return None


class FBCompany:
    """
    Dumb class gathering useful information about a fb company
    """

    def __init__(self, data):
        """
        Fills object.

        param: data: dictionary with company info
        """
        keys = ["about", "name", "website",
                "company_overview", "likes", "talking_about_count"]
        for key in keys:
            if key in data.keys():
                setattr(self, key, data[key])
            else:
                setattr(self, key, None)

    def __str__(self):
        s = "FBCompany object\n"
        for key in vars(self):
            s += " - " + key + " : " + str(getattr(self, key)) + "\n"

        return s


class FBAPI:
    """
    Interrogates Facebook API to get various pieces of information
    from pages.
    """

    def __init__(self, token):
        self.fetcher = Fetcher()
        self.logger = logging.getLogger("fbapi")
        self.token = "%s|%s" % (token["app_id"], token["secret_id"])
        self.logger.setLevel(logging.INFO)

    def get_graph(self, fburl):
        """
        Gets the graph API json from a facebook url

        param: fburl: fb page as an URL
        :returns: a string, or None if nothing found.
        """
        # Building graph URL from company page url
        # https://graph.facebook.com/datapublica
        account = get_facebook_account(fburl)
        if account is None:
            return None

        # See bug https://data-publica.atlassian.net/browse/RAD-265
        if b"\x85" in account.encode():
            return None

        url = FB_API_URL + account + "?access_token=" + self.token

        data = self.fetcher.fetch(url)
        jdata = json.loads(data.webpage)
        if "error" in jdata.keys():
            if jdata["error"]["code"] == 4:
                self.logger.warn("Rate limit exceeded")
                raise RateLimitExceeded()

            if jdata["error"]["code"] == 803:
                self.logger.warn("Couldn't find company FB page for URL %s, built into %s" %
                                 (fburl, url))
                return None

            elif jdata["error"]["code"] == 100:
                self.logger.warn("Couldn't access FB page for URL %s, badly built into %s" %
                                 (fburl, url))
                return None

            elif jdata["error"]["code"] == 104:
                self.logger.warn("Auhentification request for FB page with URL %s, built into %s" %
                                 (fburl, url))
                return None

            elif jdata["error"]["code"] == 2500:
                self.logger.warn("Unkown path to FB page with URL %s, built into %s" %
                                 (fburl, url))
                return None

            elif jdata["error"]["code"] == 12:
                self.logger.warn("Call to deprecated FB point with URL %s, built into %s" %
                                 (fburl, url))
                return None

            elif jdata["error"]["code"] == 21:
                m = re.search("to page ID (\d+).", jdata["error"]["message"])
                self.logger.info("FB page with URL %s, was migrated into %s" %
                                 (fburl, m.group(1)))
                return self.get_graph("https://graph.facebook.com/" + m.group(1))

            else:
                raise Exception(
                    "Unknown error %d : %s" % (jdata["error"]["code"], jdata["error"]["message"]))
        return jdata

    def get_company(self, fburl):
        """
        Gets a company overview from  a company facebook page.

        param: fburl: fb page as an URL
        :returns: a string, or None if nothing found.
        """

        graph = self.get_graph(fburl)
        return self.get_company_from_data(graph)

    @staticmethod
    def get_company_from_data(fbdata):
        if fbdata is None:
            return None

        return FBCompany(fbdata)

    def get_picture(self, account):
        account = get_facebook_account(account)
        if account is None:
            return None

        url = "https://graph.facebook.com/%s/picture?redirect=false&type=large" % account
        data = self.fetcher.fetch(url)
        jdata = json.loads(data.webpage)
        if "error" in jdata:
            return None
        if "data" not in jdata:
            return None
        return jdata["data"]


class FBPool:
    """
    Pool of FBAPI workers (one for each token) (to mimic TWPool).

    Requests are performed by chosing a random FBAPI and the calling the method on this
     instance.
    If the token is expired, a sleep is performed (default 60, configurable through the sleep argument).
    In case of multiple failures, 3 retries are done (default value, configurable through the retries argument).
    """
    def __init__(self, tokens):
        self.pool = [FBAPI(t) for t in tokens]

    def __getattr__(self, fun, *args, **kwargs):
        """
        See FBAPI for the methods you can call
        """

        def wrapper(*args, **kwargs):
            sleep = kwargs.pop("sleep", 60)
            retries = kwargs.pop("retries", 3)

            for i in range(retries):
                try:
                    fun_instance = getattr(random.choice(self.pool), fun)
                    return fun_instance(*args, **kwargs)
                except RateLimitExceeded:
                    time.sleep(sleep)
                    continue

        return wrapper


if __name__ == "__main__":
    logging.basicConfig(level=logging.WARNING,
                        format='[%(levelname)s][%(name)s][%(asctime)s] %(message)s')
    f = FBAPI({
        "app_id": "1647440175470290",
        "secret_id": "68445d625bbd7e34fbf1522ff14fd66d"
    })

    fbc = f.get_company("https://www.facebook.com/DataPublica")
    print(fbc)
