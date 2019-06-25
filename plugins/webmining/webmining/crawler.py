import logging
import time
import re
import urllib.parse
from queue import Empty
from multiprocessing import Process, Event
from queue import Queue as Queue
from multiprocessing import Queue as MPQueue
from threading import Thread
from enum import Enum

# Used to enable launch as a main
import os.path, sys

sys.path.insert(0, os.path.abspath('.'))

from webmining.html5wrapper import HTML5Wrapper
from webmining.seed import Seed, SeedElement
from webmining.fetcher import Fetcher, Timeout
from webmining.website import Website
from webmining.meta_extractor import MetaExtractor, MetaExtractionException
from webmining import LIB_PATH

__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"

PAGE_SIZE_LIMIT = 300000

class CrawlMode(Enum):
    #crawling an entire domain
    entire = "entire"
    #crawling a subdomain
    subpath = "subpath"
    #crawling a single page
    single = "single"

class Crawler:
    """
    A generic crawler.
    """

    def __init__(self, filename=None, seedlist=None, debug=False, proxy=None, multiproc=True,
                 mode=CrawlMode.entire, max_page_size=PAGE_SIZE_LIMIT):
        """
        :param filename: path to the seed file
        :param mode: crawling mode, either "entire", "single", "subpath"
        """
        self.seed = None
        self.debug = debug
        # init the fetcher with a download limit size
        self.fetcher = Fetcher(proxy, max_page_size=max_page_size)
        self.htmltools = HTML5Wrapper()
        self.crawl_depth = 0  # Do we crawl domains outside the seed
        self.domain_depth = 0  # At which depth each seed element must be crawled
        self.page_limit = 0  # Max amount of pages to be crawled
        self.max_page_size = max_page_size
        self.website = Website()
        self.me = MetaExtractor(proxy=proxy)

        self.badextensions = set(["pdf", "xls", "doc", "ppt", "rtf", "odt", "zip", "tar.gz", "tar", "exe", \
                                  "jpg", "png", "jpeg", "bmp", "gif", "mp3", "flv", "rar", "ogv", "avi", "mp4", \
                                  "mkg", "ps", "ogg", "webm", "ogm", "pps", "pptx", "docx", "xlsx", "mpg", "mov", \
                                  "mkv", "mpeg", "m4v", "iso"])

        self.crawling_process_over = False

        # Logging initialization
        self.logger = logging.getLogger("webmining:crawler")
        self.logger.setLevel(logging.INFO)
        if debug:
            self.logger.setLevel(logging.DEBUG)
        self.filename = filename
        self.seedlist = seedlist
        self.mode = mode
        self.authorized_domains = set()

    def _monitore_processes(self, processes):
        """
        Checks if subcrawling processes are over.
        This method is meant to be used wrapped into a Thread.
        """
        for p in processes:
            p["event"].wait()

        self.crawling_process_over = True

    def spawn_crawl_processes(self, html2txt, metas, proc, wait_courtesy):
        processes = []
        for i in range(0, proc):
            e = Event()
            p = Process(None, self._sub_crawl, None, (), {"queue": self.seed.q, "storage": self.storage, "end_event": e, \
                                                          "wait": wait_courtesy, "html2txt": html2txt, "metas": metas})
            p.start()

            processes.append({"proc": p, "event": e, "id": i})
        monitor = Thread(group=None, target=self._monitore_processes, name=None, args=(),
                         kwargs={"processes": processes})
        monitor.start()
        while not self.crawling_process_over:
            # If all processes are over, or if getting an element
            # from queue takes more than timeout seconds (which seems empirically abnormal)
            # then crawl is finished.
            c = 0
            for p in processes:
                if not p["proc"].is_alive():
                    c += 1

            if c >= len(processes):
                self.logger.warning("All processes are dead !")
                break

            try:
                el = self.storage.get(block=True, timeout=5)
                yield el
            except Empty:
                if self.storage.empty():
                    pass
        self.logger.debug("joining processes...")
        for p in processes:
            if p["proc"].is_alive():
                p["proc"].terminate()

            p["proc"].join()

        # Finally, joining monitoring thread
        monitor.join(3)
        if monitor.is_alive():
            monitor._stop()

    def crawl(self, proc=None, domain_depth=0, crawl_depth=0, page_limit=None, wait_courtesy=0, html2txt=False,
              metas=None):
        """
        :param proc:           amount of processes to spawn, 0 or None can be used to exploit the current process
        :param domain_depth:   crawling depth for each seed element (inside original domain)
        :param crawl_depth:    crawling depth for each seed element (outside original domain)
        :param page_limit:     max amount of page to crawl
        :param wait_courtesy:  time in second between each fetch
        :param html2txt:       resulting pages must be raw html (default), or cleant txt
        :param metas:          metas we want to extract during crawling
        """

        self.domain_depth = domain_depth
        self.crawl_depth = crawl_depth
        self.page_limit = page_limit
        # lazy loading, to know if we need to implement seeds with multiproc or not
        if self.seed is None:
            if self.filename is not None:
                self.seed = Seed(f=self.filename, multiproc=not (proc is None or proc == 0))
            elif self.seedlist is not None:
                self.seed = Seed(s=self.seedlist, multiproc=not (proc is None or proc == 0))

        if proc is None or proc == 0:
            self.storage = Queue()  # Will contain shared crawl results
            self._sub_crawl(self.seed.q, self.storage, Event(), wait_courtesy, html2txt, metas, None)
            while True:
                try:
                    el = self.storage.get(block=False)
                    yield el
                except Empty:
                    break
        else:
            self.storage = MPQueue()  # Will contain shared crawl results
            yield from self.spawn_crawl_processes(html2txt, metas, proc, wait_courtesy)

    def _sub_crawl(self, queue, storage, end_event, wait, html2txt, metas, block_timeout=5):
        """
        This private method will be wrapped into a process,
        and is in charge of dequeuing seed elements, and recording results into
        the storage.
        """
        while True:
            se = None
            pages = []
            try:
                se = queue.get(block=block_timeout is not None, timeout=block_timeout)
            except Empty:
                end_event.set()
                return

            self.logger.info("Launched crawl [%s]" % se.url)
            start_url = se.url # Need to keep it as it may change due to redirect
            pages = self.crawl_domain(se, self.domain_depth, wait, html2txt, self.page_limit, self.mode)
            self.logger.info("Crawl over with %d pages [%s]"
                             % (len(pages), (se.url if start_url in se.url else '%s -> %s' % (start_url, se.url))))

            first = True
            for url in pages:
                se = pages[url]
                ext_metas = {}

                # Extract asked metas from page
                if metas is not None:
                    try:
                        ext_metas = self.me.extract(metas, se.html, se.relevant_txt, \
                                                    url=url, firstpage=first)
                        first = False
                    except MetaExtractionException as e:
                        self.logger.warning("Impossible to extract metas in [%s]: " % url)
                        self.logger.warning(e)
                        continue

                    for m in ext_metas:
                        if ext_metas[m] is not None:
                            if m not in se.metas.keys():
                                if m in ["contact", "phone", "fax"]:
                                    se.metas[m] = []
                                else:
                                    se.metas[m] = set()

                            if m in ["contact", "phone", "fax"]:
                                se.metas[m].extend(ext_metas[m])
                            else:
                                se.metas[m].add(ext_metas[m])

                storage.put(se)

            # Let's save memory
            del pages

            if self.crawl_depth > 0:
                # TODO: create new seed elements to put in queue when crawl deeper than 0
                # with an updated depth, domain, etc...
                raise Exception("Not implemented")

    def _check_first_page(self, dom, url):
        """
        Checks if domain first page is
          - a html redirection
          - a frameset

        returns an url to follow, or None if nothing detected.
        """
        # we check out if it contains a <meta http-equiv="refresh"
        # ex. <meta http-equiv="Refresh" content="0; URL=corporate-finance/corporate-finance-presentation.html">
        metas = dom("meta[http-equiv='refresh'][content], meta[http-equiv='Refresh'][content], meta[http-equiv='REFRESH'][content]")
        #raise Exception("type of metas : " + str(type(metas)) + "\n" + str(dir(metas)))
        base_url = self._get_base_url(dom, url)

        for m in metas.items():
            content = m.attr.content

            m = re.search("url\s?=\s?(.*?)\s", content + ' ', flags=re.I)
            if m is not None:
                rurl = m.group(1).strip()
                rurl = urllib.parse.urljoin(base_url, rurl)

                self.logger.info("HTTP redirection to [%s]" % rurl)
                return rurl

        # We check out if it contains a <frame src="..."
        # and only return first found url if true
        # TODO: is it relevant to return only the first frame?
        frames = dom("frame[src]")
        for f in frames.items():
            rurl = urllib.parse.urljoin(base_url, f.attr.src)
            self.logger.info("FRAME redirection to [%s]" % rurl)
            return rurl

        # We check out if it contains a JS redirection document.location.href=
        # and only return first found url if true
        scripts = dom("script")
        for s in scripts.items():
            js = s.text()
            if js is not None:
                m = re.search("document.location.href\s?=\s?[\"']([^\"]*?)[\"']\s*[^+]", js + " ", flags=re.I)

                if m is not None:
                    rurl = urllib.parse.urljoin(base_url, m.group(1).strip())
                    self.logger.info("JavaScript redirection to [%s]" % rurl)
                    return rurl

        return None

    def _verify_and_parse_result(self, fresult, seed_el):
        """
        Verify if a fetch result is valid for parsing. If so, it will build the pq element that correspond to the webpage

        :param fresult: FetchResult object
        :param seed_el: SeedElement object
        :return: The pq element that correspond
        """
        if fresult is None:
            return None
        html = fresult.webpage
        content_type = fresult.content_type

        # in case of 300/302 we use final url given by fetcher
        seed_el.url = fresult.fetched_url

        if fresult.http_status is None or fresult.http_status != 200:
            self.logger.warning("Bad HTTP Status (%s) for [%s]" % (str(fresult.http_status), seed_el.url))
            return None

        if html is None:
            self.logger.warning("Impossible to crawl [%s]" % seed_el.url)
            # Missed page not ignored, as this kind of websites can be dangerous
            return None

        # We only want to compute text/html webpages
        if content_type is not None and "text/html" not in content_type.lower():
            self.logger.info("Content Type ignored : " + str(content_type) + " [" + seed_el.url + "]")
            return None

        # Too large file
        self.logger.debug("Page size of %d characters" % len(html))
        if len(html) > self.max_page_size:
            self.logger.warning("Page ignored, too big (%d characters) in %s" % (len(html), seed_el.url))
            return None

        # Is an attachment, so we must ignore it
        if fresult.attachment is not None:
            self.logger.warning(
                "Page ignored, because it correspond to the attachment %s [%s]" % (fresult.attachment, seed_el.url))
            return None

        if len(html) == 0:
            self.logger.warning("Page ignored because it is empty [%s]" % seed_el.url)
            return None

        try:
            dom = self.htmltools.pq(html)
        except Exception as e:
            self.logger.warning("Impossible to parse html url=%s : %s" % (fresult.fetched_url, str(e)))
            return None
        # DEACTIVATED FEATURE
        # Test to see if the root node is a html node
        # if dom[0].tag.lower() != 'html':
        # self.logger.warning("Page is not a valid html [%s]" % seed_el.url)
        # return None
        return dom

    @staticmethod
    def _generate_authorized_domains(domain):
        domain = domain.lower() # Force lower case
        auth = set([domain])
        if "www." in domain:
            auth.add(domain.replace("www.", ""))
        else:
            auth.add("www." + domain)

        comdom = {dom.rsplit(".", maxsplit=1)[0] + ".com" for dom in auth if ".com" not in dom}
        auth.update(comdom)

        return auth

    def _is_authorized_subpath(self, init_url, target_url):
        # Force Lower case
        init_url = init_url.lower() if init_url is not None else init_url
        target_url = target_url.lower() if target_url is not None else target_url

        init_path = urllib.parse.urlparse(init_url).path
        target_url_parsed = urllib.parse.urlparse(target_url)
        target_domain, target_path = target_url_parsed.netloc, target_url_parsed.path

        if target_domain in self.authorized_domains and target_path.startswith(init_path):
            return True
        return False

    def crawl_domain(self, init_seed_el, max_dom_depth, wait, html2txt, limit=None, mode=CrawlMode.entire):
        """
        Fetches a domain, and then crawls its internal pages until given depth.
        Returns a dictionary of url -> html code.
        """
        pages = {}
        visited = set()  # Already visited URLs
        found_links = [init_seed_el]  # List of found links as SeedElements, waiting to be fetched
        #overides the limit to crawl only one page
        if mode == CrawlMode.single:
            limit = 1
            max_dom_depth = 1
        self.logger.info("Launching crawl in the %s mode" % mode.value)
        # -- Managing authorized domains for this crawl --
        domain = urllib.parse.urlparse(init_seed_el.url).netloc
        self.authorized_domains = self._generate_authorized_domains(domain)
        self.logger.info("Authorized domains for this crawl : %s" % str(self.authorized_domains))

        # Looping through found urls
        while True:
            if limit is not None and len(visited) > limit:
                self.logger.info("Max amount of pages reached ! (%d)" % limit)
                return pages

            self.logger.debug("%d url visited so far" % len(visited))

            seed_el = None  # Current element being computed, in while loop
            try:
                while True:
                    seed_el = found_links.pop(0)
                    if seed_el.url not in visited:
                        break
                visited.add(seed_el.url)  # A popped element is considered visited
            except IndexError:
                self.logger.info("No more links to visit for this website.")
                return pages

            # Fetching URL given in seed element in param
            self.logger.debug("Fetching " + seed_el.url)

            fresult = None
            retry = 0
            max_retry = 2  # TODO - VYS - Make this configurable
            while fresult is None and retry <= max_retry:
                try:
                    fresult = self.fetcher.fetch(seed_el.url, self.debug, timeout=10)
                    # If we're here it means that no more retry are needed, disable it
                    retry = max_retry + 1
                except Timeout:
                    self.logger.warning("Timeout while fetching %s%s" % (
                    seed_el.url, (", lets retry (max retry %s)" % max_retry) if retry == 0 else (
                    " - retry %s/%s" % (retry, max_retry))))
                    retry += 1
                    continue

            if fresult is None:
                continue

            if wait > 0:
                time.sleep(wait)

            # Lets do a quick check if we don't get a redirect
            rurl30X = None
            if fresult.fetched_url != seed_el.url:
                rurl30X = fresult.fetched_url
                self.logger.warning("Got a redirect to %s when fetching %s" % (fresult.fetched_url, seed_el.url))
            dom = self._verify_and_parse_result(fresult, seed_el)
            if dom is None:
                self.logger.warning("Found no DOM for %s" % seed_el.url)
                continue

            # normalize root urls to avoid a double visit at http://www.example.com/ and http://www.example.com
            path = urllib.parse.urlparse(seed_el.url).path
            if path == '':
                seed_el.url += '/'

            self.logger.debug("Fetched [%s] " % seed_el.url)

            # If this page is the first one for this domain,
            # we check out if it contains a <meta http-equiv="refresh"
            # The same if this page is the second one,
            # because sometimes a redirection is followed by a frame
            if len(visited) < 2:
                rurl = self._check_first_page(dom, seed_el.url)
                rurl = rurl if rurl is not None else rurl30X
                if rurl is not None:
                    domain = urllib.parse.urlparse(rurl).netloc
                    domain = domain.lower()
                    # If we are following a redirect, we also add it to the set of authorized domains
                    # to be able to follow next urls.
                    self.authorized_domains.add(domain)
                    if "www." in domain:
                        self.authorized_domains.add(domain.replace("www.", ""))
                    else:
                        self.authorized_domains.add("www." + domain)

                    self.logger.info("New authorized domains for this crawl : %s" % str(self.authorized_domains))

                    if seed_el.url in visited:
                        pass
                    else:
                        visited.add(seed_el.url)

                        # Adding detected url to follow
                        ser = SeedElement(rurl, seed_el.groupid)
                        ser.depth = seed_el.depth + 1
                        found_links.append(ser)

            # If the new page url, after redirections, is outside authorized domains, don't use it
            if urllib.parse.urlparse(seed_el.url).netloc.lower() not in self.authorized_domains:
                self.logger.warning("redirection to %s don't exits from authorized domains, page not analyzed" % seed_el.url)
                continue
            if mode == CrawlMode.subpath and not self._is_authorized_subpath(init_seed_el.url, seed_el.url):
                self.logger.warning("subpath mode: redirection to %s exists from authorized subpaths, page not analyzed" % seed_el.url)
                continue
            # ---
            # HTML computing
            # ---
            # Converting html into "clean" and interesting text
            relevant_txt = self.website.extract_meaningful_text(dom)

            # Builds a new Seed Element from popped element
            se = SeedElement(seed_el.url, seed_el.groupid)
            se.depth = seed_el.depth
            se.relevant_txt = relevant_txt
            if fresult is not None:
                se.html = fresult.webpage
                se.content_type = fresult.content_type
                se.charset = fresult.charset
                se.http_status = fresult.http_status
                se.headers = fresult.headers

            # Sometimes DOM is too deep to extract title properly
            se.title = self.website.extract_title(dom)

            pages[seed_el.url] = se
            visited.add(seed_el.url)  # May be different from original, cause of redirections

            # This page has been computed, let's now extract its links
            # if ymax depth not reached
            if seed_el.depth + 1 > max_dom_depth:
                continue
            if mode != CrawlMode.single:
                found_links.extend(self._extract_links(dom, init_seed_el, seed_el, visited, mode))
           
        self.logger.debug("Out of while loop.")
        return pages

    def _get_base_url(self, dom, url):
        # check if there is a 'base' tag for link compute
        base_url = dom('base').attr('href')
        if base_url is None:
            base_url = url
        return base_url

    def _extract_links(self, dom, init_seed_el, seed_el, visited, mode):
        """
        Given a dom, extract internal links to crawl
        """

        # ---
        # Link extraction and checking
        # ---
        links = {}
        selected_links = []
        added = set()

        # DOM is sometimes to deep to extract links properly
        try:
            links = self.htmltools.extract_doc_links(dom)
        except Exception as e:
            links = {}
            self.logger.warning("Impossible to extract links from %s : %s" % (seed_el.url, str(e)))

        base_url = self._get_base_url(dom, seed_el.url)
        for key in links:
            # We do not want anchors to be crawled
            key = key.split("#")[0]
            if len(key) < 1:
                continue

            url = None
            try:
                url = urllib.parse.urljoin(base_url, key)
            except Exception as e:
                # Invalid url, ignoring
                self.logger.warning("Invalid urljoin (%s,%s): %s" % (base_url, key, str(e)))
                continue

            # Trying to get eventual file extension, and to check its validity
            path = urllib.parse.urlparse(url).path
            if path == '':
                url += '/'
            else:
                ext = path.split('.')[-1].strip().lower()
                if ext in self.badextensions:
                    self.logger.debug("Bad extension [%s] in %s" % (ext, url))
                    continue
            
            # Let's check if it's an internal link, and not an outgoing one
            if urllib.parse.urlparse(url).netloc.lower() in self.authorized_domains and \
                            url not in visited and url not in added:
                if mode == CrawlMode.subpath and not self._is_authorized_subpath(init_seed_el.url, url):
                    continue
                se = SeedElement(url, seed_el.groupid)
                se.depth = seed_el.depth + 1
                selected_links.append(se)
                added.add(url)

        return selected_links

# for testing purpose
if __name__ == "__main__":
    logging.basicConfig(level=logging.WARNING,
                        format='[%(levelname)s][%(name)s][%(asctime)s] %(message)s')
    # def crawl(self, storage, proc=1, domain_depth=0, crawl_depth=0):
    c = Crawler(LIB_PATH + "resources/testseed.txt")
    count = 0

    # def crawl(self, proc=1, domain_depth=0, crawl_depth=0, page_limit=None, wait_courtesy=0, html2txt=False, metas=None):
    for se in c.crawl(proc=2, domain_depth=2, crawl_depth=0, page_limit=80, wait_courtesy=0.1):
        # print(se)
        count += 1

    print("%d elements have been crawled !" % count)

