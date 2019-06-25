import json
from companies_plugin import extractor

from comp_screenshot.screenshot import Screenshot
from comp_screenshot import LIB_PATH


class Extractor(extractor.Extractor):
    def __init__(self, batch_name, wanted_fields, conf):
        """
        The configuration MUST have a misc called "screenshot" with the fields
        required by Screenshot

        :param batch_name:
        :param wanted_fields:
        :param conf:
        :return:
        """
        super().__init__(batch_name, wanted_fields, conf)
        self.ss = Screenshot(conf.misc["screenshot"])

    def extract(self, headers, properties, message):
        """
        Screenshot service. Take as input a {url: } document
        Returns {url: , png: <base64>}

        :param headers:
        :param properties:
        :param message:
        :return:
        """
        reply_to = properties["reply_to"]
        msg = json.loads(message)

        url = msg["url"]
        png = self.ss.shoot(url)

        result = {
            "url": url,
            "png": png
        }
        return json.dumps(result), reply_to


class Main(extractor.Main):
    pass


if __name__ == "__main__":
    m = Main(batch_name="SCREENSHOT",
             queue_name="SCREENSHOT",
             extractor_class=Extractor,
             mod_path=LIB_PATH)
    m.launch()

