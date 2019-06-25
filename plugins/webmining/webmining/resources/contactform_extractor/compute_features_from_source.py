import requests
import sys
from pyquery import PyQuery as pq
import lxml.html as LH
import os

positive_examples = ''
negative_exmaples = ''
export_filename = ''

def look_for_word(dom, word):
    if dom is not None:
        return str(LH.tostring(dom).lower()).find(word) > 0
    else:
        return False

def is_hidden_form(dom):
    inputs = [('type' in elem) and (elem.attrib['type'] == 'hidden') for elem in dom('input')]
    return False if inputs == [] else all(inputs)

def compute_features(dom):
    features = {}
    # Is "contact" present in title, h1 or form markups?
    features["contact_title"] = any([look_for_word(subdom, 'contact') for subdom in dom('title')])
    features["contact_h1"] = any([look_for_word(subdom, 'contact') for subdom in dom('h1')])
    # Are the terms "contact", "email" and "name" present in the forms (select only non-hidden forms)?
    forms = [elem[0] for elem in dom("form").items() if not is_hidden_form(elem)]
    features["contact_form"] = any([look_for_word(subdom, 'contact') for subdom in forms])
    features["email_form"] = any([look_for_word(subdom, 'email') for subdom in forms])
    features["name_form"] = any([look_for_word(subdom, 'name') for subdom in forms])
    features["nom_form"] = any([look_for_word(subdom, 'nom') for subdom in forms])
    # Are the terms "username", "login", "password", "mot de passe" present in the forms (select only non-hidden forms)?
    features["username_form"] = any([look_for_word(subdom, 'username') for subdom in forms])
    features["login_form"] = any([look_for_word(subdom, 'login') for subdom in forms])
    features["password_form"] = any([look_for_word(subdom, 'password') for subdom in forms])
    features["mdp_form"] = any([look_for_word(subdom, 'mot de passe') for subdom in forms])
    # Are the terms "recherche", "cart", "search", "commentaire" or "newsletter" present in the forms (select only non-hidden forms)?
    features["recherche_form"] = any([look_for_word(subdom, 'recherche') for subdom in forms])
    features["search_form"] = any([look_for_word(subdom, 'search') for subdom in forms])
    features["newsletter_form"] = any([look_for_word(subdom, 'newsletter') for subdom in forms])
    features["commentaire_form"] = any([look_for_word(subdom, 'commentaire') for subdom in forms])
    features["cart_form"] = any([look_for_word(subdom, 'cart') for subdom in forms])
    # Is there a textarea markup in the forms?
    features["textarea_form"] = any([look_for_word(subdom, 'textarea') for subdom in forms])
    return features

def parse_page(url):
    try:
        r = requests.get(url)
    except:
        print(url)
    return pq(r.content)

def build_data():
    global positive_examples, negative_examples, export_filename
    data = []
    data += [{"url": url.strip(), "class": "1"} for url in open(positive_examples).readlines()]
    data += [{"url": url.strip(), "class": "0"} for url in open(negative_examples).readlines()]
    
    # Export as csv
    export_file = open(export_filename, 'w')
    features = ["contact_title", "contact_h1",
                "contact_form", "email_form", "name_form", "nom_form",
                "username_form", "login_form", "password_form", "mdp_form",
                "recherche_form", "search_form", "newsletter_form", "commentaire_form", "cart_form",
                "textarea_form"]
    export_file.write(','.join(['ID'] + features + ['class\n']))
    export_file.flush()
    for i, line in enumerate(data):
        line["features"] = compute_features(parse_page(line['url']))
        export_file.write(','.join([str(i)] + [str(int(line['features'][key])) for key in features] + [line['class']]) + '\n')
        export_file.flush()
    export_file.close()



if __name__ == '__main__':
    global positive_examples, negative_examples, export_filename
        
    if len(sys.argv) == 1:
        print("Using default arguments")
        positive_examples = os.path.join(os.path.dirname(__file__), 'exemples_positifs')
        negative_examples = os.path.join(os.path.dirname(__file__), 'exemples_negatifs')
        export_filename = os.path.join(os.path.dirname(__file__), 'features.csv')
    else:
        positive_examples = sys.argv[1]
        negative_examples = sys.argv[2]
        export_filename = sys.argv[3]
    build_data()

