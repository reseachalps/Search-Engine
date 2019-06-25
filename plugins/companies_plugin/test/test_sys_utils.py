import sys
import os.path
from unittest.mock import patch, call
sys.path = [os.path.abspath("../companies_plugin"), os.path.abspath("..")] + sys.path

from companies_plugin.utils import init_stack_dumper
from companies_plugin.utils.sys_utils import DUMP_DIRECTORY, DUMP_CURRENT_PROCESS, DUMP_FILENAME
import os
from signal import SIGUSR1
from tempfile import TemporaryFile
from threading import Thread, Event


@patch("builtins.open")
@patch("os.mkdir")
def test_add_logger_to_fun(mkdir, open):
    with TemporaryFile("w+") as fake_file:
        open.return_value = fake_file

        init_stack_dumper()

        mkdir.assert_has_calls([call(DUMP_DIRECTORY), call(DUMP_CURRENT_PROCESS % {"ppid": os.getpid()})])
        # Should be pid for ppid since we are the parent process
        open.assert_called_with(DUMP_FILENAME % {"ppid": os.getpid(), "pid": os.getpid()}, "w")
        os.kill(os.getpid(), SIGUSR1)

        fake_file.seek(0)
        assert "Current thread" in fake_file.read()
        fake_file.seek(0)

        event = Event()
        t = Thread(target=event.wait)
        t.start()

        os.kill(os.getpid(), SIGUSR1)

        event.set()
        t.join()

        fake_file.seek(0)
        stack = fake_file.read()
        assert "Thread" in stack and "Current thread" in stack
