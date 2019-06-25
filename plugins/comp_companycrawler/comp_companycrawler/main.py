import json
from companies_plugin import extractor
from comp_companycrawler.website_crawler import WebsiteCrawler
from comp_companycrawler import LIB_PATH
from webmining.crawler import CrawlMode

class Extractor(extractor.Extractor):
  def __init__(self, batch_name, wanted_fields, conf):
    super().__init__(batch_name, wanted_fields, conf)

    # we do not want to use proxies for crawling, so we force it to None
    self.cr = WebsiteCrawler(proxy=None, cs=self.conf.crawl_store)

  def extract(self, headers, properties, message):
    """
    """
    reply_to = None
    results = ({}, {})

    reply_to = properties["reply_to"]
    msg = json.loads(message)
    
    #loading crawler configuration 
    depth = self.conf.crawler["depth"]
    page_max = self.conf.crawler["page_max"]
    page_size_max = self.conf.crawler["page_size_max"]

    if not msg.get("mode"):
         #overring mode to the default crawling mode.
         mode = "entire"
    else:
         mode = msg["mode"]

    report = self.cr.crawl(msg, depth=depth, page_max=page_max, mode=CrawlMode(mode), page_size=page_size_max)
    msg["count_page"] = report["countpage"]

    # Results composed of json in txt format
    # containing url identifier.
    results = (json.dumps(msg), reply_to)

    return results

class Main(extractor.Main):
  pass

if __name__ == "__main__":
  m = Main(batch_name="FOCUS_CRAWLER",
           queue_name="FOCUS_CRAWLER",
           extractor_class=Extractor,
           mod_path=LIB_PATH)
  m.launch()

