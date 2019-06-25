import re
import urllib.parse
from collections import defaultdict
from webmining import fb_api
from webmining import tw_api
from fuzzywuzzy import fuzz
from collections import namedtuple


# Represent any scored item
# data is the raw data return by the social api of the account
# Needed after scoring to extract data about the best social account.
SocialAccount = namedtuple("SocialAccount", ["account", "score", "profile_picture", "data"])


def alnum(s):
    if s is None:
        return None
    return re.sub("[\W_]", " ", s.lower())


class SocialScorer:
    # See https://data-publica.atlassian.net/wiki/display/RD/CoreExtractor+%3A+filtrage+des+comptes+sociaux
    social_thresholds = {
        "facebook": 0.5,
        "twitter": 0.6
    }

    def __init__(self, config):
        self.twitter_api = tw_api.TWPool(config["twitter"])
        self.facebook_api = fb_api.FBPool(config["facebook"])

    def score(self, domain, social, accounts):
        if social == "twitter":
            return self.twitter_score(domain, accounts)
        if social == "facebook":
            return self.facebook_score(domain, accounts)
        return {account: SocialAccount(account=account, score=0, data=None, profile_picture=None)
                for account in accounts}

    def facebook_score(self, domain, accounts):
        # Dict used to map facebook ids to a list of accounts
        # We use it later to only keep the best account for each id
        # Example: 162084397164479 and DataPublica are 2 ids to the same FB page
        # But DataPublica is probably better during a scoring
        canonical = defaultdict(list)

        scored_accounts = {}

        for account in accounts.keys():
            score = 0

            # Fetch facebook graph API page
            data = self.facebook_api.get_graph(account)
            picture_data = self.facebook_api.get_picture(account)

            if data is None or picture_data is None:
                continue
            if "id" not in data:
                continue

            # Map fb id to account
            canonical[data["id"]].append(account)

            # We prefer some types of pages
            if data.get("category") in ("Company", "Local business"):
                score += 1

            # If the website from facebook is our domain, boost
            if domain in data.get("website", ""):
                score += 1

            # Depending on the type of page, we can have a name
            name = alnum(data.get("name"))

            # Or we will use the page name from the facebook canonical link
            pagename = data.get("link") or ""
            # Sometimes it has no "alias" (fb.com/DataPublica) but a full /pages/ url
            m = re.match("https://www.facebook.com/pages/(.*)/\d+", pagename)
            if m is not None:
                pagename = m.group(1)
            else:
                m = re.match("https://www.facebook.com/(.*)", pagename)
                if m is not None:
                    pagename = m.group(1)
                    # If we still have a / in the url, we have an URL we don't know
                    # how to process. Ignore it.
                    if '/' in pagename:
                        pagename = None
                else:
                    pagename = None

            if pagename is not None:
                pagename = alnum(urllib.parse.unquote(pagename))

            # Take whatever attribute we can
            fbname = name or pagename or ""

            if "officiel" in fbname:
                score += 0.1
                fbname.replace("officiel", "")

            profile_picture = picture_data["url"] if picture_data.get("is_silhouette", True) is False else None
            if profile_picture is not None:
                score += 0.5

            ref_domain = alnum(domain)
            similarity = fuzz.partial_ratio(ref_domain, fbname) / 100

            # Filter out bad facebook accounts
            if similarity < SocialScorer.social_thresholds["facebook"]:
                continue

            score += similarity
            scored_accounts[account] = SocialAccount(account=account, score=score, data=data, profile_picture=profile_picture)

        # Keep the best account for each id
        for id, identical_accounts in canonical.items():
            if len(identical_accounts) > 1:
                scored = [(scored_accounts[a], a) for a in identical_accounts if a in scored_accounts]
                if len(scored) == 0:
                    break
                best_account = max(scored, key=lambda a: a[0].score)
                for account in identical_accounts:
                    if account != best_account[1] and account in scored_accounts:
                        del scored_accounts[account]

        return scored_accounts

    def twitter_score(self, domain, accounts):
        scored_accounts = {}

        for account in accounts.keys():
            score = 0

            data = self.twitter_api.get_user(account.replace("@", ""))

            if data is None:
                continue

            if data.get("verified", False):
                score += 1

            # attached entities: url and other stuff for accounts:
            for url in data.get("entities", {}).get("url", {}).get("urls", []):
                # if the attached url is in the domain, +1
                if domain.replace("www.", "") in url.get("display_url", ""):
                    score += 1
                    break

            ref_domain = alnum(domain)
            name = alnum(data.get("name", ""))
            screen_name = alnum(data.get("screen_name", ""))

            # Match account name and domain name
            similarity = fuzz.partial_ratio(name, ref_domain) / 100
            similarity = max(similarity, fuzz.partial_ratio(screen_name, ref_domain) / 100)

            # Filter out bad twitter accounts
            if similarity < SocialScorer.social_thresholds["twitter"]:
                continue

            score += similarity

            profile_picture = data["profile_image_url"] if data["default_profile_image"] is False else None
            if profile_picture is not None:
                score += 0.5
                profile_picture = profile_picture.replace("_normal", "")

            scored_accounts[account] = SocialAccount(account=account, score=score, data=data, profile_picture=profile_picture)

        return scored_accounts
