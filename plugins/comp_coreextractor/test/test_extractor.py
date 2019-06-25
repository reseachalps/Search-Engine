import sys
import os.path
sys.path = [os.path.abspath("../comp_coreextractor")] + sys.path
sys.path = [os.path.abspath("..")] + sys.path


from comp_coreextractor.social_scorer import SocialScorer
import requests_mock
import json

conf = {
    "twitter": [
        {"key": "PYPbLc6FQ3iSyE5JBMlXkYrCC", "secret": "GM7xhSAd4vFgNdO2bcuAjDF551EEvF0alhKp8tFqiKv9opQKYY"},
        {"key": "eCkXVhcCtoUn52QevdENOfk2M", "secret": "snn0bPO3mOE0VqOo952HzFfsVy6J5x61x7X2296nZ6YdDjJDrK"}
    ],
    "facebook": [
        {"app_id": "1", "secret_id": "1"}
    ]
}


def test_facebook_social_scoring():

    with requests_mock.mock() as m:
        ss = SocialScorer(conf)

        m.get("https://graph.facebook.com/v2.4/DataPublica?access_token=1|1", text=json.dumps({
            "category": "Company",
            "website": "http://data-publica.com/",
            "name": "Data Publica",
            "id": "4"
        }))

        m.get("https://graph.facebook.com/DataPublica/picture?redirect=false&type=large", text=json.dumps({
            "data": {
                "url": "http://i.imgur.com/HJYmCLN.png",
                "is_silhouette": False
            }
        }))

        m.get("https://graph.facebook.com/v2.4/DataPublic?access_token=1|1", text=json.dumps({
            "category": "Company",
            "website": "http://data-publicool.com/",
            "name": "Data Publicool",
            "id": "4"
        }))

        m.get("https://graph.facebook.com/DataPublic/picture?redirect=false&type=large", text=json.dumps({
            "data": {
                "url": "http://i.imgur.com/HJYmCLN.png",
                "is_silhouette": True
            }
        }))

        accounts = {"http://facebook.com/DataPublica": None, "http://facebook.com/DataPublic": None}

        print(ss.facebook_score("data-publica.com", accounts))
        assert(len(ss.facebook_score("data-publica.com", accounts).keys()) == 1)
        assert(ss.facebook_score("data-publica.com", accounts)["http://facebook.com/DataPublica"].score == 3.5)
