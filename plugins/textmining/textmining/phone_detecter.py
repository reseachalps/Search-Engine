import re
import phonenumbers as pn


class PhoneDetecter:
    """
    Tries to detect and extract phone numbers from raw text
    """

    def _categorize_phone(self, txt, match):
        """
        :param txt: raw txt containing the match
        :param match: PhoneNumberMatch to categorize
        """

        begin = match.start - 20
        if begin < 0:
            begin = 0
        subtxt = txt[begin:match.end]
        if "fax" in subtxt.lower() or "télécopie" in subtxt.lower():
            return "fax"
        return "phone"

    def detect(self, txt, country="FR"):
        """
        :param txt: the text where we will try to find phone numbers
        :param country: the country format of the phone
        """

        txt = re.sub("\s", " ", txt)

        return [(self._categorize_phone(txt, match), pn.format_number(match.number, pn.PhoneNumberFormat.INTERNATIONAL))
                for match in pn.PhoneNumberMatcher(txt, country) if pn.is_valid_number_for_region(match.number, country)]
