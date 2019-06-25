import logging

from webmining import meta_extractor
from webmining.fetcher import Proxy
from cstore_api import crawl_store
from textmining import wf_histogram
from comp_coreextractor.webentity import WebEntity
from comp_coreextractor.social_scorer import SocialScorer
from comp_coreextractor.descriptions import get_best_facebook_account_description, get_best_twitter_account_description

from comp_coreextractor import LIB_PATH


class CoreExtractor:

    """
    Basic extraction from html pages, in common with all projects.
    """

    def __init__(self, proxy, cs, metas, social):
        """
        param: proxy: dict to build a proxy object for stealth fetching
        param: cs: dict to configure elastic search (crawl store)
        param: metas: group metas to extract
        param: social configuration used to score social accounts
        """

        self.cs = crawl_store.CrawlStore(conf=cs)
        self.metas = metas
        if proxy is not None and "url" in proxy:
            self.proxy = Proxy(
                url=proxy["url"], username=proxy["username"], password=proxy["password"])
        else:
            self.proxy = None
        self.me = meta_extractor.MetaExtractor(proxy=self.proxy)

        # Logging initialization
        self.logger = logging.getLogger("core_extractor")
        self.logger.setLevel(logging.INFO)
        file_handler = logging.FileHandler(
            filename=LIB_PATH + "../log/log.txt")
        file_handler.setFormatter(
            logging.Formatter('[%(levelname)s][%(name)s][%(asctime)s] %(message)s'))

        self.logger.addHandler(file_handler)
        self.social_scorer = SocialScorer(social)

    @staticmethod
    def _init_entity_report(entity):
        report = WebEntity()
        report["summary"] = wf_histogram.WFHistogram()
        report["url"] = entity["url"]
        report["domain"] = entity["domain"]
        report["country"] = entity["country"]
        report["ecommerce_meta"] = {"pages_with_prices": 0, "delivery_options": set(), "payment_options": set()}

        return report

    def extract(self, entity, depth=2, page_max=80):
        """
        Gather information about an entity
        """

        self.logger.info("Website : %s" % entity["url"])
        pages = self.cs.get_crawl_by_url(entity["url"], depth=depth, max_pages=page_max)
        self.logger.info("%d pages for crawl of url %s" % (len(pages), entity["url"]))

        entity_report = self._init_entity_report(entity)
        firstpage = True

        desc = None
        counter = 0
        for p in pages:
            html = p.content
            relevant_txt = p.relevant_txt
            metas = self.extract_metas(html, relevant_txt, p.url, firstpage, country=entity["country"], lang=p.lang)
            firstpage = False

            # Tagcloud
            if relevant_txt is not None:
                entity_report["summary"].add_text(relevant_txt)

            # Metas
            for m in metas:
                if metas[m] is not None:
                    counter += 1

                    # -- Special cases initialization --
                    if m == "prices":
                        if len(metas[m]) > 0:
                            entity_report["ecommerce_meta"]["pages_with_prices"] += 1
                            entity_report["prices_per_page"].append(len(metas[m]))

                    if m in ["payment_options", "delivery_options"]:
                        if len(metas[m]) > 0:
                            entity_report["ecommerce_meta"][m].update(metas[m])

                    if m == "contact":
                        for cont in metas[m]:
                            cont.depth = p.depth
                            cont.source = p.url

                    # -- General cases __
                    if m in entity_report.str_attributes:
                        entity_report[m] = metas[m]
                    elif m in entity_report.social_attributes:
                        # For now, meta extractor returns only one social
                        # account / page
                        entity_report[m][metas[m]] = 0
                    elif m in entity_report.list_attributes:
                        if type(metas[m]) != list:
                            # Manages values returned 'alone' but wen want to aggregate
                            metas[m] = [metas[m]]
                        for el in metas[m]:
                            entity_report[m].append(el)
                    elif type(metas[m]) == list:
                        for el in metas[m]:
                            entity_report[m].add(el)
                    else:
                        if type(metas[m]) == list or type(metas[m]) == set:
                            entity_report[m].update(metas[m])
                        else:
                            entity_report[m].add(metas[m])

            # Checking if seems to be homepage
            if p.url.strip(" /") == "http://" + p.domain.strip(" /"):
                # Checking if seems to be homepage:
                if "description" in metas.keys() and metas["description"] is not None:
                    desc = metas["description"]

        # end for p in pages
        entity_report.normalize(len(pages))

        social_accounts = {}
        for social in entity_report.social_attributes:
            social_accounts[social] = self.social_scorer.score(domain=entity_report["domain"],
                                                               social=social,
                                                               accounts=entity_report.get(social, {}))

            # Format social fields for insertion in DB
            entity_report[social] = social_accounts[social]

        # Metadesc is always the description from the website
        if desc is not None:
            entity_report["metadescription"] = desc

        # Get the best account’s description for each social network
        fb_desc = get_best_facebook_account_description(social_accounts.get("facebook"))
        tw_desc = get_best_twitter_account_description(social_accounts.get("twitter"))

        # Put all descs we collected in a list
        # We filter out very long descriptions (666 is a serious measure).
        descs = [d for d in (desc, fb_desc, tw_desc) if d is not None and len(d) < 666]
        # When meta desc AND social desc are available, the longest
        # is usually the best one.
        descs = sorted(descs, key=lambda d: len(d), reverse=True)
        desc = None if len(descs) == 0 else descs[0]

        if desc is not None:
            entity_report["description"] = desc

        # Aggregate extracted outlink domains
        if "outlinks" in entity_report.keys():
            agg = {}
            for (domain, count) in entity_report["outlinks"]:
                if domain is not None:
                    if domain in agg:
                        agg[domain] += 1
                    else:
                        agg[domain] = 1
            entity_report["outlinks"] = agg

        self.logger.info("Computed Website [%s] - %d metas extracted" %
                         (entity["domain"], counter))

        return entity_report

    def extract_metas(self, cleanhtml, relevant_txt, url, firstpage, country, lang):
        ext_metas = {}

        # Extract asked metas from page
        try:
            ext_metas = self.me.extract(self.metas, cleanhtml, relevant_txt,
                                        url, firstpage, country=country, lang=lang)
        except meta_extractor.MetaExtractionException as e:
            self.logger.warn("Impossible to extract metas\n %s" % str(e))

        return ext_metas
