# Read the documentation here:
#  https://nose.readthedocs.org/en/latest/testing.html

# Modify the import path to find our package
import sys
import os.path
sys.path = [os.path.abspath("../textmining")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path


# Import our package
from textmining import LIB_PATH
from textmining.contact_detecter import ContactDetecter
from textmining.normalizer import Normalizer
from textmining.wf_histogram import WFHistogram


def test_wf_histogram():
  # test plurals
  wf = WFHistogram()
  wf_nl = WFHistogram(locale="NL")
  text = "pommes bleues pomme bleue"
  text_nl = "appels kinderen appel kinder"
  wf.add_text(text)
  wf_nl.add_text(text_nl)
  assert len(wf.freq) == 2
  assert len(wf_nl.freq) == 2


def test_normalizer():
  N = Normalizer()

  # tests phone numbers normalization
  tels = ["06 23 57 68 56", "+33515253549", "+33 551283870", "06.23.57.68.65"]
  be_tels = ["02 513 89 40", "+32010230312","0473 55 12 83"]
  expected = ["+33 6 23 57 68 56", "+33 5 15 25 35 49", "+33 5 51 28 38 70", "+33 6 23 57 68 65"]
  be_expected = ["+32 2 513 89 40", "+32 10 23 03 12", "+32 473 55 12 83"]
  
  count = 0
  for tel in tels:
    normed = N.normalize_phone_number(tel)
    if normed != expected[count]:
      raise Exception("Expected %s - got %s"% (expected[count], normed))

    count += 1
  
  count = 0
  for tel in be_tels:
    normed = N.normalize_phone_number(tel, country="BE")
    if normed != be_expected[count]:
        raise Exception("Expected %s - got %s" % (be_expected[count], normed))
    count += 1

  # tests date normalization
  dates = ["20110504 12:00", "09092011", "2011-02-25 16:00"]
  expected = ["04/05/2011", "09/09/2011", "25/02/2011"]

  count = 0
  for d in dates:
    normed = N.normalize_date(d)
    if normed != expected[count]:
      raise Exception("Expected %s - got %s"% (expected[count], normed))

    count += 1


def test_contact_detecter():
  txt = open( LIB_PATH + "./resources/testcontact.txt" ).read()
  # Contacts to be found
  ref = ["Thierry GIBERT, Président Directeur Général",
         "Jean-Hubert SCHMITT, Directeur Général Délégué",
         "Philippe DUBEAUX, Directeur Exécutif",
         #"Camille Rayon, President",
         #"Pierre Rayon, Managing Director",
         #"Patrick Lara, Harbour Master",
         #"Yves Benard, Port Officers",
         "Alain VICENTE, Directeur Commercial",
         "Pascal PETEL, Responsable",
         "Philippe VALLEE, Responsable Commercial",
         "Virginie Mabille, Webmaster",
         #"Patrick BANLIAT, Regie publicitaire",
         "Stephanie TESSIER, Comptabilite",
         #"LAURENT Loic, Impression",
         "Pierre VAN, Publication",
         #"Gerard PIEL, Gerant",
         #"Camille Rayon, Managing Director",
         "Corinne Valentin, Responsable",
         "Yvan RANDON, fondateur  SOFIRAN",
         "Guillaume Dard, Président Directeur Général",
         "Guillaume Dard, Président",
         "Serge Abiteboul, Directeur",
         "Nathalie Andrieux, Directrice",
         "Michel Briand, Directeur Adjoint",
         "Virginia Cruz, designer  IDSL",
         "Pascal Daloz, Directeur Général Adjoint",
         "Marie Ekeland, Associé",
         "Virginie Fauvel, Directrice",
         "Cyril Garcia, Directeur",
         "Audrey Harris, Président Directeur Général",
         "Francis Jutand, Directeur Scientifique",
         "Daniel Kaplan, Délégué Général",
         "Tariq Krim, Président Directeur Général",
         "Tristan Nitot, Président",
         "Sophie Pene, professeur a lUniversite",
         "Valerie Peugeot, chercheuse a Orange",
         "Nathalie Pujo, Directrice",
         "Jean-Baptiste Rudelle, Président",
         "Cecile Russeil, Directrice",
         "Nathalie Sonnac, professeur  sciences",
         "Bernard Stiegler, Directeur",
         "Marc Tessier, Administrateur",
         "Benoit Thieulin, Directeur",
         "Brigitte Vallee, Directrice",
         "Frederic Combier, Directeur",
         "empty"]

  # Launching detecter
  cd = ContactDetecter( firstnames=LIB_PATH + "resources/firstnames.txt",
                        positions=LIB_PATH + "resources/positions.txt")

  # Checking positions were loaded properly
  assert(len(cd.positions) == 79)
  assert(len(cd.ref_positions) == 895)

  detected = cd.detect( txt )
  # Now checking detection is alright
  i = 0
  for c in detected:
    print( "> Trying [%s] / [%s]" % (ref[i], str(c)) )
    #print( "> [%s]" % str(c) )
    assert( str(c) == ref[i] )
    i += 1

  print( "passed %d tests ok." % i )
  assert(i == len(ref) - 1)
