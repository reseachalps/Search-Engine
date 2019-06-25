import faulthandler
import atexit
import signal
import traceback
import os
import multiprocessing

# To test with doctest...
if not __package__:  # pragma: no cover
    LIB_PATH = "."
else:
    from .. import LIB_PATH


# Global configuration
DUMP_DIRECTORY = LIB_PATH + "/dump/"
DUMP_CURRENT_PROCESS = DUMP_DIRECTORY + "%(ppid)s/"
DUMP_FILENAME = DUMP_CURRENT_PROCESS + "%(pid)s.dump"


def get_stack():  # pragma: no cover # nothing to test here
    """Returns the current stack trace as a string."""
    return traceback.format_exc()


class StackDumper:
    """
    Dumps the stack of the current process and all of its threads when it receives a SIGUSR1 signal.
    The stack will go in dump/<main process id>/<real process id>.dump file.

    You do not need to create this object yourself, instead, use `init_stack_dumper()`
    """
    def __init__(self):
        try:
            os.mkdir(DUMP_DIRECTORY)
        except:  # pragma: no cover # if it already exists
            pass

        ppid = os.getpid() if multiprocessing.current_process().name == "MainProcess" else os.getppid()

        dir = DUMP_CURRENT_PROCESS % {"ppid": ppid}
        try:
            os.mkdir(dir)
        except:  # pragma: no cover # if it already exists
            pass

        self.fname = DUMP_FILENAME % {"ppid": ppid, "pid": os.getpid()}
        self.f = open(self.fname, "w")
        self.fname = self.f.name  # The actual filename, can change when we patch open for testing
        faulthandler.register(signal.SIGUSR1, self.f, chain=False)

        atexit.register(self.clean)

    def clean(self):  # pragma: no cover # not detected since it's called at exit
        faulthandler.unregister(signal.SIGUSR1)
        if not self.f.closed:
            self.f.close()
            if os.stat(self.fname).st_size == 0:
                os.unlink(self.fname)


def init_stack_dumper():
    """
    Initalize the stack dumper for current process.
    This method should be called for all subprocesses (but not for threads)

    >>> import os
    >>> from signal import SIGUSR1
    >>> init_stack_dumper()
    >>> # From your terminal send the SIGUSR1 signal
    >>> # kill -SIGUSR1 <pid>
    >>> os.kill(os.getpid(), SIGUSR1)  # send the SIGUSR1 signal
    >>> print(open(DUMP_FILENAME % dict(ppid=os.getpid(), pid=os.getpid()), "r").read())  # doctest: +ELLIPSIS
    Current thread ... (most recent call first):...
    """
    StackDumper()
