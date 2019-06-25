import sys
import os.path
sys.path = [os.path.abspath("../textmining")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path

from textmining import phone_detecter


def test_phone_be():
    pos = {
        "+32(0) 1 234 56 78": "+32 12 34 56 78",
        "+32 01 234 56 78": "+32 12 34 56 78",
        "+32 012 34 56 78": "+32 12 34 56 78",
        "+32 1 234 56 78": "+32 12 34 56 78",
        "012 34 56 78": "+32 12 34 56 78",
        "01 234 56 78": "+32 12 34 56 78",
        "Tel. : 01 234 56 78": "+32 12 34 56 78",
        "01 234 56 78 - 1234": "+32 12 34 56 78",
        "012345678": "+32 12 34 56 78",
        "01-234-56-78": "+32 12 34 56 78",
        "012-34-56-78": "+32 12 34 56 78",
        "01.234.56.78": "+32 12 34 56 78",
        "01-234-56-78": "+32 12 34 56 78",
    }

    neg = [
        "000 00 00 00",  # Should have a non zero digit in second position
        "+33 1 23 45 67 89",  # Not from belgium
        "012 34 56 78 0",  # Too long
    ]

    det = phone_detecter.PhoneDetecter()
    for txt, exp in pos.items():
        found = det.detect(txt, country="BE")
        assert found == [("phone", exp)], (
            "For string %s, found %s, expected %s" % (txt, found, [exp]))

    for txt in neg:
        found = det.detect(txt, country="BE")
        assert len(found) == 0, (
            "For string %s, found %s, expected nothing" % (txt, found))


def test_fax_be():
    pos = {
        "fax: +32(0) 1 234 56 78": "+32 12 34 56 78",
        "FAX +32 01 234 56 78": "+32 12 34 56 78",
        "fAx +32 012 34 56 78": "+32 12 34 56 78",
        "fax +32 1 234 56 78": "+32 12 34 56 78",
        "het is onze fax: 012 34 56 78": "+32 12 34 56 78",
    }

    neg = [
        "fax 000 00 00 00",  # Should have a non zero digit in second position
        "het is niet goede faxe +33 1 23 45 67 89",  # Not from belgium
        "fax dat ofwel 012 34 56 78 0",  # Too long
    ]

    det = phone_detecter.PhoneDetecter()
    for txt, exp in pos.items():
        print(txt)
        found = det.detect(txt, country="BE")
        assert found == [("fax", exp)], (
            "For string %s, found %s, expected %s" % (txt, found, [exp]))

    for txt in neg:
        found = det.detect(txt, country="BE")
        assert len(found) == 0, (
            "For string %s, found %s, expected nothing" % (txt, found))


def test_phone_fr():
    pos = {
        "01 23 45 67 89": "+33 1 23 45 67 89",
        "0123456789": "+33 1 23 45 67 89",
        "+331 23 45 67 89": "+33 1 23 45 67 89",
        "+33 1 23 45 67 89": "+33 1 23 45 67 89",
        "+33(0) 1 23 45 67 89": "+33 1 23 45 67 89",
        "hey tu me files ton 06? ok: 06 23 45 67 89.": "+33 6 23 45 67 89",
        "Tél. : 04 74 45 38 31 ": "+33 4 74 45 38 31",
        "0123456789 - 1234": "+33 1 23 45 67 89",
        "01.23.45.67.89 - 1234": "+33 1 23 45 67 89",
    }
    neg = [
        "00 00 00 00 00",  # Should have a non-zero second digit
        "01 23 45 67 89 0",  # Too long
        "09à10h40-12h20",
        "+32 1 23 45 67 89",  # Not french
    ]

    det = phone_detecter.PhoneDetecter()
    for txt, exp in pos.items():
        found = det.detect(txt)
        assert found == [("phone", exp)], (
            "For string %s, found %s, expected %s" % (txt, found, [exp]))

    for txt in neg:
        found = det.detect(txt)
        assert len(found) == 0, (
            "For string %s, found %s, expected nothing" % (txt, found))


def test_fax_fr():
    pos = {
        "FAX: 01 23 45 67 89": "+33 1 23 45 67 89",
        "fax : 0123456789": "+33 1 23 45 67 89",
        "fax:+331 23 45 67 89": "+33 1 23 45 67 89",
        "blahblah fax : +33 1 23 45 67 89": "+33 1 23 45 67 89",
        "fax : +33(0) 1 23 45 67 89": "+33 1 23 45 67 89",
    }
    neg = [
        "fax 00 00 00 00 00",  # Should have a non-zero second digit
        "FaX :01 23 45 67 89 0",  # Too long
    ]

    det = phone_detecter.PhoneDetecter()
    for txt, exp in pos.items():
        found = det.detect(txt)
        assert found == [("fax", exp)], (
            "For string %s, found %s, expected %s" % (txt, found, [exp]))

    for txt in neg:
        found = det.detect(txt)
        assert len(found) == 0, (
            "For string %s, found %s, expected nothing" % (txt, found))


def test_phone_and_fax_fr():
    det = phone_detecter.PhoneDetecter()
    found = det.detect("tel: 0123456780 FAX: 01 23 45 67 89")
    print(found)
    assert found == [("phone", "+33 1 23 45 67 80"), ("fax", "+33 1 23 45 67 89")]


def test_phone_and_fax_be():
    det = phone_detecter.PhoneDetecter()
    found = det.detect("tel: 01 234 56 78, fax: +32 1 234 56 78", country="BE")
    print(found)
    assert found == [("phone", "+32 12 34 56 78"), ("fax", "+32 12 34 56 78")]
