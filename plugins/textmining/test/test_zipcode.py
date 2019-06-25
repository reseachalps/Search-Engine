import sys
import os.path
sys.path = [os.path.abspath("../textmining")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path

from textmining import extractor

def test_zipcode_price():
  e = extractor.Extractor()

  txt = "un prix de 20 000€"
  ans = e.extract_zipcode(txt)
  assert(ans is None)

  txt = "un prix de 55000 euros"
  ans = e.extract_zipcode(txt)
  assert(ans is None)

def test_zipcode():
  e = extractor.Extractor()

  txt = "le vote dans le département 75010 "
  ans = e.extract_zipcode(txt)
  assert(ans == "75010")
  
  txt = "à paris, dans le 75 010 les gens sont des bobos. "
  ans = e.extract_zipcode(txt)
  assert(ans == "75010")

def test_badzipcode():
  e = extractor.Extractor()

  txt = "Intitulé : b> Guer (56) - Lycée BrocéliandeOP130032 -"
  ans = e.extract_zipcode(txt)
  assert(ans is None)

  txt = " OP130032 Lycée Brocéliande "
  ans = e.extract_zipcode(txt)
  assert(ans is None)

  txt = " OP B130032 - Lycée "
  ans = e.extract_zipcode(txt)
  assert(ans is None)

  txt = " OP B130032 - Lycée Brocéliand e"
  ans = e.extract_zipcode(txt)
  assert(ans is None)
  
  txt = "Votre annonce n°14-61007 est maintenant prise en compte"
  ans = e.extract_zipcode(txt)
  assert(ans is None)

  txt = "à paris, dans le CS-75010 les gens sont des bobos. "
  ans = e.extract_zipcode(txt)
  assert(ans is None)

def test_bp_vs_zipcode():
  e = extractor.Extractor()

  txt = "CC Terrasses et vallée de l'Aveyron. Correspondant : le président, maison de l'intercommunalité, 370 avenue du 8 mai 1945 B.P. 80035 82800 Negrepelisse tél. : 05-63-30-90-90 télécopieur : 05-63-30-81-77 courriel : adresse internet : . Adresse internet du profil d'acheteur : ." 
  ans = e.extract_zipcode(txt)
  assert(ans == "82800")

  txt = "CC Terrasses et vallée de l'Aveyron. Correspondant : le président, maison de l'intercommunalité, 370 avenue du 8 mai 1945 C.S. 80035 82800 Negrepelisse tél. : 05-63-30-90-90 télécopieur : 05-63-30-81-77 courriel : adresse internet : . Adresse internet du profil d'acheteur : ." 
  ans = e.extract_zipcode(txt)
  assert(ans == "82800")

