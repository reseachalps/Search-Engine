import json
from companies_plugin import extractor

from scanr_entityextractor.namedentitydetecter import NamedEntityDetecter
from scanr_entityextractor import LIB_PATH


class Extractor(extractor.Extractor):
    def __init__(self, batch_name, wanted_fields, conf):
        super().__init__(batch_name, wanted_fields, conf)
        self.ned = NamedEntityDetecter(conf.api, conf.crawl_store)
        
    def extract(self, headers, properties, message):
        """
        Detects entities in a given url's text

        :param headers:
        :param properties:
        :param message:  A message with the structure {message:{...}, url:"http://"}
        :return: A message with the structure
            {message:{...}, entities: [{id: "ANR-443-FR", pos: [0,2]}]}
        """

        reply_to = properties["reply_to"]
        msg = json.loads(message)

        result = self.ned.detect_all(msg["url"])

        result = [{"id": r["id"][0], "type":r["id"][1]} 
                 for r in result ]
        reply = { "url": msg["url"], 
                  "entities": result }
        
        return reply, reply_to


class Main(extractor.Main):
    pass


if __name__ == "__main__":
    m = Main(batch_name="NAMED_ENTITY_DETECTER",
             queue_name="NAMED_ENTITY_DETECTER",
             extractor_class=Extractor,
             mod_path=LIB_PATH)
    m.launch()

