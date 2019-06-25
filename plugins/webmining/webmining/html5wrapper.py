import lxml
from lxml.html import HtmlComment
import re

from pyquery import PyQuery as pq


__author__ = "Guillaume Lebourgeois"
__email__ = "guillaume.lebourgeois@data-publica.com"
__status__ = "dev"


class HTML5Wrapper():
    """
    LXML parser used in pq and lxml methods.
    It will only parse utf-8 strings
    """
    parser = lxml.html.HTMLParser(encoding='utf-8')


    def pq(self, html, remove_script=False):
        """
        Builds a pq object from the html string

        :param html: the utf-8 html string
        :returns: the pq object
        """
        if remove_script:
            return pq(self.lxml(html)).remove("script")

        return pq(self.lxml(html))

    def lxml(self, html):
        return lxml.html.fromstring(html.encode('utf-8'), parser=self.parser)

    def absolute_xpath(self, node):
        "Generate an absolute xpath from a node."
        return pq(node[0]).root.getpath(node[0])

    def highlight_node(self, pqnode, color):
        """
          Highlight a node, surrounding it with 5px borders of the given color
        """

        node = pqnode[0]
        if node.tag == "tbody":
            node = node.getparent()

        if node.tag == "tr":
            pqnode = pqnode.children()

        pqnode.css.border = "5px solid %s" % color

    def backgroundize_node(self, pqnode, color):
        """
          Highlight a node, adding it a background of the given color
        """
        node = pqnode[0]
        if node.tag == "tbody":
            node = node.getparent()

        if node.tag == "tr":
            pqnode = pqnode.children()

        pqnode.css.background_color = color

    def extract_doc_links(self, doc, tags={"a", "area"}):
        """
        Extracts all hyperlinks from a dom document.
        Returns a dictionary with link as key, and value as a tuple (dom element, link text).
        """
        # return {l.attr.href: (l, l.text())
        # for l in doc('a,area').items()
        #     if l.attr.href is not None
        # }
        links = dict()
        for l in doc[0].iterlinks():
            if l[0].tag in tags:
                r = pq(l[0])
                links[l[2]] = (r, r.text())
        return links

    newline_tags = {"br", "p", "div", "li", "ul", "ol", "table", "tr", "td", "h1", "h2", "h3", "h4", "h5", "blockquote"}
    useless_tags = {"script", "style", "head"}

    def get_text_from_subtree(self, node, exception=None):
        """
        Merges text nodes inside a node. May be used for example
        to generate merged text inside a <p> tag.
        Useless tags as script, style, ... are ignored.
        <br>, <p> and such introduce a supplementary '\n'
        """
        useless_tags = self.useless_tags
        if exception is not None:
            useless_tags = useless_tags - {exception}

        spaces = re.compile(r'\s+')

        def gettext(node):
            nltag = node.tag in self.newline_tags
            txt = ""
            if nltag:
                txt += "\n"
            if node.text is not None and not isinstance(node, HtmlComment):
                text = re.sub(spaces, ' ', node.text)
                txt += text
            for child in node.iterchildren():
                if child.tag not in useless_tags:
                    txt += gettext(child)
            if nltag:
                txt += "\n"
            if node.tail is not None:
                text = re.sub(spaces, ' ', node.tail)
                txt += text
            return txt

        text = "".join([gettext(n) for n in node])

        return self.normalize_whitespace(text)

    def normalize_whitespace(self, text):
        text = text.strip()

        # space normalization
        text = re.sub(r'\r', '', text)
        text = re.sub(r'\s*\n\s*', '\n', text)
        text = re.sub(r'[ \t]+', ' ', text)
        return text

    text = get_text_from_subtree

    def clean_html(self, html_text):
        """
        Takes a raw html string, and converts it into a good html string,
        well encoded.
        """
        return lxml.etree.tostring(self.lxml(html_text), encoding='utf-8').decode('utf-8')

    def html(self, pq_elt):
        """
        Takes a pq elt
        """
        return lxml.etree.tostring(pq_elt[0], encoding='utf-8').decode('utf-8')
