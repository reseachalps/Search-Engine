import sys
import os.path
sys.path = [os.path.abspath("../textmining")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path

from textmining.stemmer import Stemmer


def test_normalize_text():
    s = Stemmer()

    txt = "J'ai acheté du pain, des croissants & des chocolatines ; ça fait beaucoup ! Bla. Bla ? \ a | b - c ( d ) e"
    ans = s.normalize_text(txt)
    print("[" + ans + "]")
    assert(ans == "J ai acheté du pain des croissants des chocolatines ça fait beaucoup Bla Bla a b - c d e")


def test_strip_accents_and_lower():
    s = Stemmer()

    txt = "Château café IRENE ça arène Noël"
    ans = s.strip_accents_and_lower(txt)
    print("[" + ans + "]")
    assert(ans == "chateau cafe irene ca arene noel")


def test_stem_word():
    s = Stemmer()

    txt = "mayenne cassoulet nature la a ? ! 34?"
    ans = " ".join([s.stem_word(t) for t in txt.split()])
    print("[" + ans + "]")
    assert(ans == "mayen cassoulet natur     ")


def test_stem_text():
    s = Stemmer()

    txt = "J'ai acheté du pain, des croissants & des chocolatines ; ça fait beaucoup ! Bla. Bla ? \ a | b - c ( d ) e"
    ans = " ".join(s.stem_text(txt))
    print("[" + ans + "]")
    assert(ans == "achet pain croiss chocolatin beaucoup bla bla")


def test_compute_stem_hist():
    s = Stemmer()

    txt = "chateau Châteaux chateaux exemple EXEMPLE exemples EXEMPLES exemple"
    ans = s.compute_stem_hist(txt)
    print("[" + str(ans) + "]")
    assert(sum(ans["chateau"].values()) == 3 and sum(ans["exempl"].values()) == 5)


def test_compute_nice_stem_hist():
    s = Stemmer()

    txt = "chateau Châteaux Châteaux chateaux exemple EXEMPLE exemples EXEMPLES exemple"
    ans = s.compute_nice_stem_hist(txt)
    print("[" + str(ans) + "]")
    assert(ans["Châteaux"] == 4 and ans["exemple"] == 5)


def test_get_complete_vocabulary():
    s = Stemmer()

    corpus = [s.compute_stem_hist(text) for text in ["chateau Châteaux Châteaux chateaux exemple EXEMPLE exemples EXEMPLES exemple", "Château café IRENE ça arène Noël", "J'ai acheté du pain, des croissants & des chocolatines ; ça fait beaucoup !"]]
    ans = s.get_complete_vocabulary(corpus)
    print("[" + str(ans) + "]")
    assert(ans == {'achet', 'chateau', 'beaucoup', 'chocolatin', 'croiss', 'aren', 'iren', 'pain', 'noel', 'caf', 'exempl'})


def test_flatten_stem_corpus():
    s = Stemmer()

    corpus = [s.compute_stem_hist(text) for text in ["chateau Châteaux Châteaux chateaux exemple EXEMPLE exemples EXEMPLES exemple", "Château café IRENE ça arène Noël", "J'ai acheté du pain, des croissants & des chocolatines ; ça fait beaucoup !"]]
    ans = s.flatten_stem_corpus(corpus)
    print("[" + str(ans) + "]")
    assert(ans == [{'chateau': 4, 'exempl': 5}, {'caf': 1, 'chateau': 1, 'aren': 1, 'iren': 1, 'noel': 1, }, {'croiss': 1, 'beaucoup': 1, 'achet': 1, 'chocolatin': 1, 'pain': 1}])


def test_languages():
    # If you read this test, you should try listening the zaaiuien word : translate.google.com/#nl/fr/zaaiuien
    s = Stemmer()
    txt = "zaaiuien"

    nl_hist = s.compute_stem_hist(txt, "NL")
    fr_hist = s.compute_stem_hist(txt, "FR")

    assert "zaaiui" in nl_hist and "zaaiui" not in fr_hist


def test_normalize_text_with_digits():
    s = Stemmer(keep_digits=True)

    txt = "J'ai acheté 1 pain, 3 croissants & 4 chocolatines ; ça fait beaucoup ! Bla. Bla ? \ a | b - c ( d ) e"
    ans = s.normalize_text(txt)
    print("[" + ans + "]")
    assert(ans == "J ai acheté 1 pain 3 croissants 4 chocolatines ça fait beaucoup Bla Bla a b - c d e")
