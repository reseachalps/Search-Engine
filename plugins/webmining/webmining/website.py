import logging
import html as html_parser
import re

# Used to enable launch as a main
import os.path
import sys

sys.path.insert(0, os.path.abspath('.'))
from webmining.html5wrapper import HTML5Wrapper
import webmining.relevant_text_extractor as relevant_text_extractor


__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"


class Website:
    def __init__(self):
        """
        Initializes a website extractor.
        """
        self.htmlwrapper = HTML5Wrapper()
        self.logger = logging.getLogger("webmining.Website")

    def magnify(self, html):
        """
        param: html: raw html
        returns: tuple(clean html, relevant text)
        """
        # Try to get dom from html string
        try:
            dom = self.htmlwrapper.pq(html)
        except Exception as e:
            self.logger.warning("Impossible to magnify HTML\n %s" % str(e))
            return None, None

        # Converting html into "clean" and interesting text
        relevant_txt = self.extract_meaningful_text(dom)
        return dom, relevant_txt

    def extract_text(self, html):
        """
        Transforms HTML into nice text.

        param: html: raw html
        :returns: text as a string
        """
        try:
            dom = self.htmlwrapper.pq(html)
            return self.htmlwrapper.text(dom("body"))
        except Exception as e:
            self.logger.warning("Impossible to extract text with parser, "
                                "using dumb method.\n %s" % str(e))
            cleantxt = html_parser.unescape(html)
            cleantxt = re.sub("<.+?>", " ", cleantxt, flags=re.S)

            return cleantxt

    def extract_title(self, dom):
        """
        Extract title from a webpage, if not found or error it will always return ""
        """
        try:
            return self.htmlwrapper.text(dom("title"))
        except Exception:
            return ""

    def extract_meaningful_text(self, dom):
        """
        Extract meaningful content from an html page. Returns raw text.
        param: dom: the pq element
        """
        try:
            return relevant_text_extractor.extract(dom)
        except Exception as e:
            # Extraction can fail when dom is too deep
            # We then try a dumb méthod with a regexp
            self.logger.warning("Impossible to extract meaningful text, using "
                                "dumb method.\n %s" % str(e))
            return self.htmlwrapper.text(dom("body"))
