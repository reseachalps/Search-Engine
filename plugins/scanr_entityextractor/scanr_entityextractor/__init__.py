import os.path
__version__ = open(os.path.dirname(os.path.abspath(__file__)) +
                   "/version.txt").read().strip()

# Path to lib directory at runtime
LIB_PATH = os.path.dirname(os.path.abspath(__file__)) + "/"


def some_fun():
    print("OK")
