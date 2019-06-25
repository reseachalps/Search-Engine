from webmining.fb_api import FBAPI


def best_social(accounts):
    if accounts is None or len(accounts) < 1:
        return None
    accounts = [(account, data) for account, data in accounts.items()]
    accounts = sorted(accounts, key=lambda a: a[1].score, reverse=True)
    return accounts[0]


def get_best_facebook_account_description(accounts):
    account = best_social(accounts)
    if account is None:
        return None

    (account, data) = account[0], account[1].data

    fbc = FBAPI.get_company_from_data(data)

    if fbc is None:
        return None

    if fbc.company_overview is not None:
        return fbc.company_overview.strip()

    if fbc.about is not None:
        return fbc.about.strip()

    return None


def get_best_twitter_account_description(accounts):
    account = best_social(accounts)
    if account is None:
        return None

    (account, data) = account[0], account[1].data

    if "description" in data:
        return data["description"]

    return None
