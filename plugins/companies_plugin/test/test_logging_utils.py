import sys
import os.path
from unittest.mock import patch, call
from nose.tools import raises
sys.path = [os.path.abspath("../companies_plugin"), os.path.abspath("..")] + sys.path

from companies_plugin.utils import add_logger, add_timer, add_logger_file, add_timer_msg
from companies_plugin.utils.logging_utils import TIME_LOGGING_FORMAT


@patch("logging.getLogger")
@patch("logging.Logger")
def test_add_logger_to_fun(Logger, getLogger):
    pouet_logger = Logger("pouet")
    getLogger.return_value = pouet_logger

    @add_logger
    def pouet(logger=None):
        assert logger is not None
        logger.info("test")

    pouet()

    getLogger.assert_called_with("pouet")
    pouet_logger.info.assert_called_with("test")


@patch("logging.getLogger")
@patch("logging.Logger")
def test_add_logger_to_class(Logger, getLogger):
    pouet_logger = Logger("Pouet")
    getLogger.return_value = pouet_logger

    @add_logger
    class Pouet:
        def pouet(self):
            assert self.logger is not None
            self.logger.info("test")

    Pouet().pouet()

    getLogger.assert_called_with("Pouet")
    pouet_logger.info.assert_called_with("test")


@patch("logging.getLogger")
@patch("logging.Logger")
def test_add_logger_file(Logger, getLogger):
    pouet_logger = Logger("pouet")
    getLogger.return_value = pouet_logger

    @add_logger_file("pouet.txt")
    def pouet(a, logger=None):
        assert logger is not None
        logger.info(a + " test")

    pouet("canard")
    getLogger.assert_called_with("pouet")
    pouet_logger.info.assert_called_with("canard test")


@patch("time.time")
@patch("builtins.print")
def test_add_timer(print, time):
    time.side_effect = [1, 2]

    @add_timer
    def pouet():
        pass

    pouet()
    time.assert_has_calls([call(), call()])
    print.assert_has_calls([call(TIME_LOGGING_FORMAT % {"name": "pouet", "time": 1})])


@patch("time.time")
@patch("builtins.print")
def test_add_timer_msg(print, time):
    time.side_effect = [1, 2]
    msg = "pouet %(time).2f"

    @add_timer_msg(msg)
    def pouet():
        pass

    pouet()
    time.assert_has_calls([call(), call()])
    print.assert_has_calls([call(msg % {"name": "pouet", "time": 1})])


@raises(NotImplementedError)
def test_add_logger_unknown_type():
    add_logger("a")
