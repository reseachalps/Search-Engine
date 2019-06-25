import logging
import os
import types
import time

# To test with doctest...
if not __package__:  # pragma: no cover
    LIB_PATH = "."
else:
    from .. import LIB_PATH


# Global configuration
LOGGING_FORMAT = '[%(levelname)s][%(name)s][%(asctime)s] %(message)s'
LOGGING_DIRECTORY = LIB_PATH + "/log/"
LOGGING_FILE = LOGGING_DIRECTORY + "log.txt"

TIME_LOGGING_FORMAT = "Spent time in method %(name)s: %(time).2fs"

# Set some logging stuff
logging.basicConfig(level=logging.WARNING,
                    format=LOGGING_FORMAT)
try:
    os.mkdir(LOGGING_DIRECTORY)
except OSError as e:
    pass

# Global variable to track the name of loggers already configured
_logged_classes = set()


def _create_logger(logger_name, logfile, level=logging.INFO):
    logger = logging.getLogger(logger_name)
    logger.setLevel(level)

    if logger_name not in _logged_classes:
        file_handler = logging.FileHandler(filename=LOGGING_FILE)
        file_handler.setFormatter(
            logging.Formatter(LOGGING_FORMAT))
        logger.addHandler(file_handler)
        if logfile is not None:
            file_handler = logging.FileHandler(filename=LOGGING_DIRECTORY + logfile)
            file_handler.setFormatter(
                logging.Formatter(LOGGING_FORMAT))
            logger.addHandler(file_handler)
        _logged_classes.add(logger_name)

    return logger


def _add_logger_to_class(cls, logfile=None):
    old_init = cls.__init__

    def new_init(self, *args, **kwargs):
        # Logging initialization
        log_level = kwargs.pop("log_level", logging.INFO)

        self.logger = _create_logger(cls.__name__, logfile, log_level)

        # Normal init
        old_init(self, *args, **kwargs)

    cls.__init__ = new_init
    return cls


def _add_logger_to_function(function, logfile=None):
    logger = _create_logger(function.__name__, logfile)

    def wrapper(*args, **kwargs):
        kwargs["logger"] = logger
        return function(*args, **kwargs)
    return wrapper


def add_logger(anything, logfile=None):
    """
    Adds a logger to a class or a function.
    The logged content is appended to a file named `LOGGING_FILE`.

    To decorate a class, use it as an attribute:

    >>> @add_logger
    ... class Test:
    ...     def test(self, a):
    ...         self.logger.info(a)
    >>> Test().test(1)

    To decorate a function, add an argument named logger.
    >>> @add_logger
    ... def some_fun(arg1, arg2=1, logger=None):
    ...     logger.info(arg1 + arg2)
    >>> some_fun(1, 2)

    To use the additional parameter `logfile`, see @`add_logger_file`
    """
    if isinstance(anything, type):
        return _add_logger_to_class(anything, logfile)
    if isinstance(anything, types.FunctionType):
        return _add_logger_to_function(anything, logfile)
    raise NotImplementedError("No logger can be added to type %s" % type(anything))


def add_logger_file(logfile):
    """
    Adds a logger to a class or a function, appending the content to a file named `logfile`.
    See @add_logger for additionnal behaviour.
    """
    def _decorator(cls):
        return add_logger(cls, logfile)
    return _decorator


def add_timer(method_or_fun, msg=None):
    """
    Time the decorated function.

    Without an accessible logger, it uses print to display the time spent in the function.
    >>> @add_timer
    ... def some_fun():
    ...     pass
    >>> some_fun()
    Spent time in method some_fun: 0.00s

    Else it combines with @add_logger to dump the time through a logger.

    You can specify a custom log message by using @`add_timer_msg`.
    """
    if msg is None:
        msg = TIME_LOGGING_FORMAT

    def wrapper(*args, **kwargs):
        if isinstance(method_or_fun, types.FunctionType):
            # Case of a function with a logger attribute (injected by add_logger)
            logging_func = kwargs.get("logger", None)
            # Case of a method call where the first argument is `self`, and `self` contains a logger attribute
            if logging_func is None and len(args) > 1:
                logging_func = getattr(args[0], "logger", None)

        if logging_func is None or not hasattr(logging_func, "info"):
            logging_func = print
        else:
            logging_func = logging_func.info

        start_time = time.time()
        ret = method_or_fun(*args, **kwargs)
        logging_func(msg % {"name": method_or_fun.__name__, "time": (time.time() - start_time)})

        return ret

    return wrapper


def add_timer_msg(msg):
    """
    Adds a timer with a custom message
    See @`add_timer` for additionnal behaviour.
    """
    def _decorator(method_or_fun):
        return add_timer(method_or_fun, msg)
    return _decorator
