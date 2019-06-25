from collections import deque
from datetime import datetime, timezone, timedelta


class Cache:
    def __init__(self, expiration=timedelta(hours=24)):
        """
        This cache manager guarantees that a (reference, object) entry will only exist for a specific amount of time.
        Cleaning is made for each call of get.

        :param expiration: A timedelta instance that corresponds to the allowed time span
        """

        self.cache = {}
        self.expiration = expiration
        # FIFO queue of (datetime, reference), ordered by datetime ASC by construction
        self.expiration_queue = deque()

    def get(self, reference):
        """
        Get the object corresponding to this reference

        :param reference: The reference
        :return: The corresponding object or None if not found (or expired)
        """

        # Clean everything
        self._clean()

        if reference not in self.cache:
            return None

        return self.cache[reference]

    def put(self, reference, obj):
        """
        Put a reference with its object in the cache. This will be available for two hours.

        Restriction: this call will raise an Exception if the user try to override a reference
                     that is already in the cache.

        :param reference: The reference
        :param obj: The object
        """

        if reference in self.cache:
            raise Exception("Trying to erase an unexpired cache entry (should not be useful in our case)")
        self.cache[reference] = obj
        self.expiration_queue.append([Cache._now(), reference])

    def _clean(self):
        """
        Remove expired entries in the cache.
        """

        max_datetime = Cache._now() - self.expiration
        while len(self.expiration_queue) > 0 and self.expiration_queue[0][0] <= max_datetime:
            t, reference = self.expiration_queue.popleft()
            del self.cache[reference]

    @staticmethod
    def _now():
        """
        Returns the current timestamp (extracted for testing purposes)

        :return: a utc aware datetime
        """
        return datetime.now(timezone.utc)
