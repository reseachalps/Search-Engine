import sys
import os.path
sys.path = [os.path.abspath("../textmining")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path

from textmining.address_detecter import AddressDetecter
from textmining import LIB_PATH


def test_address_be():
    with open(os.path.join(LIB_PATH, "resources/testaddressbe.txt")) as f:
        data = f.read()
    data = data.split("---")

    to_be_found = [
        'rue Dante 75',
        'Van Schoonhovenstraat 21-23',
        'Rue Osseghem 53',
        'Chaussée de Wavre 136',
        'Schaarbeeklei 198',
        'Avenue du Port 86c',
        'Keizer Karellaan 28',
        'Lied van Sotternieenlaan 1',
    ]
    to_be_found = [e.lower() for e in to_be_found]

    a = AddressDetecter(debug=True)
    for t in data:
        print("Expected: %s" % t)
        addr = a.detect_address(t, html=True, country="BE")
        print(addr)
        if addr is None or addr.address not in to_be_found:
            raise Exception("ERROR - unexpected [%s]" % str(addr))


def test_address_detecter():
    f = open(LIB_PATH + "resources/testaddress.txt")
    txt = f.read()
    txts = txt.split("---")

    addresses = ["52 AVENUE DE HAMBOURG",
                 "37 rue de Dampierre",
                 "10, Rue de la Bouverie",
                 "57 boulevard de l'embouchure",
                 "9, rue Boierie",
                 "11 Boulevard des Récollets",
                 "zi la chartreuse",
                 "zac du césar",
                 "8 Chemin du Gué d'Arancé",
                 "10, lotissement Casanova",
                 "31, rue de la Convention",
                 "za de la maîtrie",
                 "335 route de Vannes",
                 "za de l'hermillon",
                 "113 RUE ISAMBARD",
                 "zac belle aire nord rue léonard",
                 "4A, rue du Général Leclerc",
                 None]
    addresses = [e.lower() if e is not None else None for e in addresses]

    a = AddressDetecter(debug=True)
    count = 0
    for t in txts:
        if addresses[count] is None:
            continue
        addr = a.detect_address(t, html=True)
        if addr is None or addr.address != addresses[count]:
            print("ERROR - %s extracted instead of %s" % (str(addr), str(addresses[count])))
            raise Exception("ERROR - [%s] extracted instead of [%s]" % (str(addr), str(addresses[count])))
        else:
            print("OK - %s" % str(addr))

        count += 1

def test_detect_addresses():
    f = open(LIB_PATH + "resources/testaddresses.txt")
    txt = f.read()
    addresses = ["52 AVENUE DE HAMBOURG",
                 "37 rue de Dampierre",
                 "10, Rue de la Bouverie",
                 "57 boulevard de l'embouchure",
                 "9, rue Boierie",
                 "11 Boulevard des Récollets",
                 "zi la chartreuse",
                 "zac du césar",
                 "8 Chemin du Gué d'Arancé",
                 "10, lotissement Casanova",
                 "31, rue de la Convention",
                 "za de la maîtrie",
                 "335 route de Vannes",
                 "za de l'hermillon",
                 "113 RUE ISAMBARD",
                 "zac belle aire nord rue léonard",
                 "4A, rue du Général Leclerc",
                 "31 boulevard Amiral-Bruix",
                 "zi seclin rue de la pointe",
                 "6, passage Saint-Ambroise"]
    addresses = [e.lower() for e in addresses]

    a = AddressDetecter(debug = True)
    set_detct_addrs = set()
    set_addrs = set(addresses)

    detct_addrs = a.detect_addresses(txt, html=True)
    print([e.__dict__ for e in detct_addrs])
    assert len(detct_addrs) == len(addresses), "Detected %d addresses instead of %d" % (len(detct_addrs), len(addresses))

    for ad in detct_addrs:
        set_detct_addrs.add(ad.address)

    set_forgotten = set_addrs - set_detct_addrs
    for token in set_forgotten:
        assert False, "ERROR - %s not extracted properly" % str(token)

def test_voie_detecter():
    addresses = ["52 AVENUE DE HAMBOURG",
                 "37 rue de Dampierre",
                 "10, Rue de la Bouverie",
                 "57 boulevard de l'embouchure",
                 "9, rue Boierie",
                 "11 Boulevard des Récollets",
                 "8 Chemin du Gué d'Arancé",
                 "31, rue de la Convention",
                 "335 route de Vannes",
                 "113 RUE ISAMBARD",
                 "4A, rue du Général Leclerc",
                 "rue sans numero",
                 "plein de choses avant rue",
                 "  rue sans numero mais avec espace avant",
                 "plein de choses avant et des espaces apres rue  ",
                 ]

    a = AddressDetecter(debug=True)
    for address in addresses:
        assert a.detect_voie(address) is not None, (
            "%s should contain a voie" % address)


def test_city_detecter():
    cities = {
        "Angers rox": "angers",
        "PARIS is magic": "paris",
        "Saint-Léon-sur-Vézère": "saint leon sur vezere",
        "St Léon sur Vézère": "st leon sur vezere",
        "Flavigny-sur-Ozerain": "flavigny sur ozerain",
        "Bourg-En-Bresse": "bourg en bresse",
    }

    a = AddressDetecter(debug=True)
    for city, exp in cities.items():
        det = a.detect_city(city)
        assert det == [exp], (
            "%s should contain a city (%s != %s)" % (city, det, exp))
