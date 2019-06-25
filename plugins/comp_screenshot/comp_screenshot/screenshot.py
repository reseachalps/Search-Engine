import logging
import subprocess
import os
from comp_screenshot import LIB_PATH


class Screenshot:
    def __init__(self, screen_conf):
        """
        Init the screenshot service

        :param screen_conf: A dict with the following keys,
                    "phantom": the path to the phantomjs binary
                    "width", "height": The desired width / height
                                        of the viewport
                    "loadDelay": The delay in ms after which the screenshot is
                                 taken
                    "timeout": The maximum time in ms that a phantom instance
                                can take until being destroyed
        :return:
        """
        self.exe = screen_conf["phantom"]
        self.load_delay = screen_conf["load_delay"]
        self.width = screen_conf["width"]
        self.height = screen_conf["height"]
        self.timeout = screen_conf["timeout"] / 1000

        self.logger = logging.getLogger("screenshot")
        self.logger.setLevel(logging.INFO)

        # Check out if phantom is available
        if not os.path.exists(self.exe):
            self.logger.error("Phantom JS environement is not available")
            raise Exception("Phantom JS environement is not available")

    def shoot(self, url):
        """
        Take a screenshot and return a base64 binary png.

        :param url:
        :return: The base64 png or None if any error has happened
                    (Timeout or Error while executing phantom)
        """

        self.logger.info("Rendering url " + url)
        try:
            # Prepare args
            args = [self.exe, LIB_PATH + "/script/rasterize.js", url,
                    self.width, self.height, self.load_delay]
            args = [str(a) for a in args]
            out = subprocess.check_output(args, timeout=self.timeout)

            # Decode the binary output
            # Strip is here to remove the \n at the end.
            out = out.decode('ascii').strip()
            # Make sure we get only the base64 result
            out = out.split("\n")[-1]

            self.logger.info("Successfully rendered url " + url)
            return out

        # Phantom has crashed OR it can't open the website
        except subprocess.CalledProcessError as e:
            self.logger.warn("Unable to render url " + url)
            return None

        # The process takes too long and has been killed
        except subprocess.TimeoutExpired as e:
            self.logger.warn(
                "Timeout reached while rendering url " + url)
            return None
