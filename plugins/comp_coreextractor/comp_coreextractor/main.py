import json
from companies_plugin import extractor
from comp_coreextractor.core_extractor import CoreExtractor
from comp_coreextractor import LIB_PATH


class Extractor(extractor.Extractor):

    def __init__(self, batch_name, wanted_fields, conf):
        super().__init__(batch_name, wanted_fields, conf)
        self.metas = ["Communication", "Social", "Structure"]
        self.metas.extend(["Monitoring", "CMS", "Shopping", "eCommerce"])
        # Will fail if no configuration provided. Made intentionally, will be removed when new crawl store be there 
        self.depth = self.conf.misc["coreextractor"]["depth"]
        self.page_max = self.conf.misc["coreextractor"]["page_max"]

        self.ce = CoreExtractor(proxy=None, cs=self.conf.crawl_store, metas=self.metas, social=self.conf.social)

    def extract(self, headers, properties, message):
        """
        """
        results = ({}, {})

        reply_to = properties["reply_to"]
        msg = json.loads(message)

        # Launching crawler on collected object
        report = self.ce.extract(msg, depth=self.depth, page_max=self.page_max)
        if report is None:
            return results

        # Preparing results to send them
        msg.update(report.export())
        results = (msg, reply_to)

        return results


class Main(extractor.Main):
    pass

if __name__ == "__main__":
    m = Main(batch_name="CORE_EXTRACTOR",
             queue_name="CORE_EXTRACTOR",
             extractor_class=Extractor,
             mod_path=LIB_PATH)
    m.launch()
