# Common stuff for social APIs

class RateLimitExceeded(Exception):
    # Constructor accepts anything
    # Needed to have nice stack traces
    # (they are displayed with arguments replaced
    #  by their values)
    def __init__(self, *args, **kwargs):
        pass
