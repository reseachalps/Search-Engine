import re
import logging
import base64
import uuid
import time
import urllib.parse as urlparse
import hmac
import requests
import datetime
from webmining.social_apis import RateLimitExceeded
import random


class TWAPI:
    """
    Interrogates Twitter API to get various pieces of information
    from pages.
    """

    def __init__(self, config):
        self.logger = logging.getLogger("twapi")
        self.logger.setLevel(logging.INFO)
        self.consumer_key = config["key"]
        self.consumer_secret = config["secret"]

    def _gen_oauth_params(self):
        nonce = base64.b64encode(str(uuid.uuid4()).encode())
        nonce = re.sub("\W", "", nonce.decode())
        timestamp = str(int(time.time()))
        oauth_token = ""
        oauth_version = "1.0"
        oauth_method = "HMAC-SHA1"
        params = {
            "oauth_consumer_key": self.consumer_key,
            "oauth_nonce": nonce,
            "oauth_signature_method": oauth_method,
            "oauth_timestamp": timestamp,
            "oauth_version": oauth_version,
            "oauth_token": oauth_token,
        }
        return params

    def _sign(self, method, url, request_params):
        oauth_params = self._gen_oauth_params()

        digest_params = dict(oauth_params)
        digest_params.update(request_params)

        digest_params = list(digest_params.items())
        digest_params.sort()

        params = [urlparse.quote(key) + "=" + urlparse.quote(val)
                  for key, val in digest_params]
        params = "&".join(params)

        msg = '&'.join([method.upper(), urlparse.quote_plus(url), urlparse.quote(params)])

        hkey = urlparse.quote(self.consumer_secret) + "&"
        digest = hmac.new(hkey.encode(), msg.encode(), "SHA1")
        signature = base64.b64encode(digest.digest())

        oauth_params["oauth_signature"] = signature

        return oauth_params

    def _request(self, method, url, params):
        url = "https://api.twitter.com/1.1/" + url
        oauth = self._sign(method, url, params)
        authorization = list(oauth.items())
        authorization.sort()
        authorization = ['%s="%s"' % (urlparse.quote(key), urlparse.quote(val))
                         for key, val in authorization]
        authorization = ', '.join(authorization)
        authorization = "OAuth " + authorization

        data = None
        if method.upper() == "GET":
            url += "?" + urlparse.urlencode(list(params.items()))
        else:
            data = urlparse.urlencode(list(params.items()))

        response = requests.request(method, url, data=data, headers={"Authorization": authorization})
        if response.status_code != 200:
            if response.status_code == 429:
                now = response.headers["date"]
                reset = datetime.datetime.fromtimestamp(int(response.headers["x-rate-limit-reset"])).isoformat()
                raise RateLimitExceeded(now, reset)
            print(response.status_code)
            print(response.content)
            return None
        return response.json()

    def get_user(self, account):
        """
        Gets the user data for a given account
        """
        return self._request("GET", "users/show.json", {"screen_name": account})


class TWPool:
    """
    Pool of TWAPI workers (one for each token).
    Requests are performed by chosing a random TWAPI and the calling the method on this
     instance.
    If the token is expired, a sleep is performed (default 60, configurable through the sleep argument).
    In case of multiple failures, 3 retries are done (default value, configurable through the retries argument).
    """
    def __init__(self, tokens):
        self.pool = [TWAPI(t) for t in tokens]

    def __getattr__(self, fun, *args, **kwargs):
        """
        See TWAPI for the methods you can call
        """

        def wrapper(*args, **kwargs):
            sleep = kwargs.pop("sleep", 60)
            retries = kwargs.pop("retries", 3)

            for i in range(retries):
                try:
                    fun_instance = getattr(random.choice(self.pool), fun)
                    return fun_instance(*args, **kwargs)
                except RateLimitExceeded:
                    time.sleep(sleep)
                    continue

        return wrapper


if __name__ == "__main__":
    logging.basicConfig(level=logging.WARNING,
                        format='[%(levelname)s][%(name)s][%(asctime)s] %(message)s')
    t = TWAPI({"key": "PYPbLc6FQ3iSyE5JBMlXkYrCC",
               "secret": "GM7xhSAd4vFgNdO2bcuAjDF551EEvF0alhKp8tFqiKv9opQKYY"})
    dp = t.get_user("@maserati_hq")
    import json
    print(json.dumps(dp, indent=2))
