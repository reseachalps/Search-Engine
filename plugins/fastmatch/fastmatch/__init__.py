import os.path
from fastmatch.matcher import Matcher
__version__ = open(os.path.dirname(os.path.abspath(__file__)) +
                   "/version.txt").read().strip()

# Path to lib directory at runtime
LIB_PATH = os.path.dirname(os.path.abspath(__file__)) + "/"

__all__ = (Matcher,)
