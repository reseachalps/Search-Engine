__author__ = 'Clément Chastagnol'

import lxml.html as LH


class ContactFormExtractor:
    """
    Detects if a page contains a contact form from a variety of computed features.
    The detection uses a linear model
    """


    def __init__(self, dom):
        self.features = dict()
        self.dom = dom

    # Internal method: looks for the presence of a word (as a substring) in a dom
    def _look_for_word(self, dom, word):
        if dom is not None:
            return str(LH.tostring(dom)).lower().find(word) > 0
        else:
            print('Problem with dom:', LH.tostring(dom))
            return False

    # Internal method: checks if a form is hidden (i.e. has all its "input" with
    # attribute "type" as "hidden")
    def is_hidden_form(self, dom):
        inputs = [('type' in elem) and (elem.attrib['type'] == 'hidden') for elem in dom('input')]
        return False if inputs == [] else all(inputs)

    def compute_features(self):
        """
        Computes all the necessary features on the dom (most features check for the presence
        of at least one word in a given element of the dom)
        """
        self.features = dict()
        # Is "contact" present in title, h1 or form markups?
        self.features["contact_title"] = any([self._look_for_word(subdom, 'contact') for subdom in self.dom('title')])
        self.features["contact_h1"] = any([self._look_for_word(subdom, 'contact') for subdom in self.dom('h1')])
        # Are the terms "contact", "email" and "name" present in the forms?
        # (select only non-hidden forms, presence of at least one occurence)
        forms = [elem[0] for elem in self.dom("form").items() if not self.is_hidden_form(elem)]
        self.features["contact_form"] = any([self._look_for_word(subdom, 'contact') for subdom in forms])
        self.features["email_form"] = any([self._look_for_word(subdom, 'email') for subdom in forms])
        self.features["name_form"] = any([self._look_for_word(subdom, 'name') for subdom in forms])
        self.features["nom_form"] = any([self._look_for_word(subdom, 'nom') for subdom in forms])
        # Are the terms "username", "login", "password", "mot de passe" present in the forms?
        # (select only non-hidden forms, presence of at least one occurence)
        self.features["username_form"] = any([self._look_for_word(subdom, 'username') for subdom in forms])
        self.features["login_form"] = any([self._look_for_word(subdom, 'login') for subdom in forms])
        self.features["password_form"] = any([self._look_for_word(subdom, 'password') for subdom in forms])
        self.features["mdp_form"] = any([self._look_for_word(subdom, 'mot de passe') for subdom in forms])
        # Are the terms "recherche", "cart", "search", "commentaire" or "newsletter" present in the forms?
        # (select only non-hidden forms, presence of at least one occurence)
        self.features["recherche_form"] = any([self._look_for_word(subdom, 'recherche') for subdom in forms])
        self.features["search_form"] = any([self._look_for_word(subdom, 'search') for subdom in forms])
        self.features["newsletter_form"] = any([self._look_for_word(subdom, 'newsletter') for subdom in forms])
        self.features["commentaire_form"] = any([self._look_for_word(subdom, 'commentaire') for subdom in forms])
        self.features["cart_form"] = any([self._look_for_word(subdom, 'cart') for subdom in forms])
        # Is there a textarea markup in the forms?
        self.features["textarea_form"] = any([self._look_for_word(subdom, 'textarea') for subdom in forms])

    def predict(self):
        """
        Decides if the page has a contact form or not
        """
        if not self.features.values():
            self.compute_features()

        # These coefficients come from a linear regression model trained on manually annotated examples.
        coefficients = [0.29110108, 0.06726632, -0.16432598, -0.04445764,
                        0.04630792, 0.00252617, -0.21805663, -0.1165878,
                        0.21138187, 0.1408565, -0.05743341, -0.0164482,
                        0.06128956, -0.67672961, 0.00250222, 0.81270421]
        # Keys with a fixed order, because calling self.features.keys() is not guaranteed to always
        # return the keys in the same order
        keys = ["contact_title", "contact_h1", "contact_form", "email_form",
                "name_form", "nom_form", "username_form", "login_form",
                "password_form", "mdp_form", "recherche_form", "search_form",
                "newsletter_form", "commentaire_form", "cart_form", "textarea_form"]
        # The decision is the weighted sum of the features. We get a classification decision by
        # thresholding the computed prediction.
        decision = sum([c * int(self.features[k]) for c,k in zip(coefficients, keys)])
        return bool(round(decision))