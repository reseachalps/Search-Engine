import re
import codecs
import string

from requests.compat import chardet
import magic
from webmining.encoding_labels import LABELS


# Regex for identifying charsets declared in meta tags in HTML pages
# example : <meta charset="utf-8"/> or <meta content="text/html; charset=utf-8"/>
META_REGEX = re.compile(b"<meta[^>]{,140}? charset=[\"']?(.{,20}?)(?=[\"/ ';>])", re.I | re.M)


def auto_detect(text):
    charset = None

    # Try a rapid detection using filemagic
    with magic.Magic() as m:
        magic_print = m.id_buffer(text)

    # UTF-8 is rapidly detected, but if the result is not UTF-8,
    # we use the more precise, but slower, chardet.
    if magic_print is not None and "UTF-8" in magic_print:
        charset = "utf-8"
    else:
        charset = chardet.detect(text)['encoding']

    if charset is not None:
        charset = charset.lower()

    return charset


def detect_meta_charset(html):
    """
    Try to find the charset declared in a meta tag.
    """

    match = META_REGEX.search(html)

    if match is not None:
        charset = str(match.group(1), encoding="ascii", errors="replace")
        charset = charset.replace("\"", "").lower()
        charset = "".join([s for s in charset if s in string.printable])
        try:
            codecs.lookup(charset)
        except LookupError:
            return None
        return charset

    else:
        return None


def strip_html(html):
    """ Remove inline CSS, JS and tags from an HTML byte string """

    # Remove superfluous whitespace
    text = re.sub(b"\s+", b" ", html)
    # Remove inline css
    text = re.sub(b"<style.*?>.*?</style>", b"", text)
    # Remove inline javascript
    text = re.sub(b"<script.*?>.*?</script>", b"", text)
    # Remove tags
    text = re.sub(b"<.+?>", b"", text)

    return text


def detect_charset(text, html=True, default=None):
    """
    Given a bytes text, returns a guess at what that text’s charset is.
    If the text is assumed to be html and no charset is declared,
    all inline CSS, JS and tags are removed for a faster detection.
    param: text
    kwparam: html: wheter the text is assumed to be html, defaults to true
    """

    if html:
        # We first try to detect the declared encoding, if not declared,
        # we try auto-detection after removing useless html.
        declared = detect_meta_charset(text)

        if declared is not None:
            return declared

        text = strip_html(text)

    detected = auto_detect(text)

    if detected is None:
        return default
    else:
        try:
            real = LABELS[detected]
            return real
        except KeyError:
            return None


def encode_text(text, charset):
    # Code from requests
    # Decode unicode from given encoding.
    try:
        text = str(text, charset, errors='replace')
    except (LookupError, TypeError):
        # A LookupError is raised if the encoding was not found which could
        # indicate a misspelling or similar mistake.
        #
        # A TypeError can be raised if encoding is None
        #
        # So we try blindly encoding.
        text = str(text, errors='replace')

    return text
