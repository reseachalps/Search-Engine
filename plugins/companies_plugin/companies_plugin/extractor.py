import json
import os
import getopt
import sys
import traceback
import signal
import logging


# Used to enable launch as a main
import os.path
sys.path.insert(0, os.path.abspath('.'))
from companies_plugin.website_queue import WebsiteQueue


class Configuration:
    """
    Configuration object, filled from a
    json conf file.
    """
    def __init__(self):
        self.rabbit = None
        self.crawl_store = {}
        self.search_engine = {}
        self.database = {}
        self.proxy = {}
        self.api = {}
        self.social = {}
        self.crawler = {}
  
        # nestor corpus files when machine learning needed
        self.nestor = {}
        self.bing_api_key = {}
        self.plugin = {}

        # miscellaneous configuration
        self.misc = {}

    def load(self, path):
        with open(path) as f:
            conf = json.load(f)
            for key, value in conf.items():
                if key == "bing_api_key":
                    self.bing_api_key = conf["bing_api_key"]
                elif key == "rabbit":
                    self.rabbit = {}
                    for k in ["hostname", "port", "username", "password"]:
                        self.rabbit[k] = value[k]
                elif key == "crawl_store":
                    for k in ["hostnames", "keyspace"]:
                        self.crawl_store[k] = value[k]
                elif key == "search_engine":
                    for k in ["hostname", "port", "index"]:
                        self.search_engine[k] = value[k]
                elif key == "database":
                    for k in ["hostname", "port", "username", "password", "database"]:
                        self.database[k] = value[k]
                elif key == "proxy":
                    for k in ["url", "username", "password"]:
                        self.proxy[k] = value[k]
                elif key == "api":
                    for k in ["base_url", "username", "password"]:
                        self.api[k] = value[k]
                elif key == "nestor":
                    self.nestor = value
                elif key == "social":
                    self.social = value
                elif key == "plugin":
                    for k in ["max_mem"]:
                        self.plugin[k] = conf["plugin"][k] 
                elif key == "crawler":
                     for k in ["depth", "page_max", "page_size_max"]:
                         self.crawler[k] = value[k]
                else:
                    self.misc[key] = value

        if self.rabbit is None:
            raise Exception("Rabbit configuration is mandatory for all companies engine plugins.")


class Main:

    def __init__(self, batch_name, queue_name, extractor_class, mod_path, max_mem=None):
        """
        param: batch_name: global name for this plugin
        param: queue_name: rabbit queue name for this plugin
        param: extractor_class: class of this plugin extractor
        param: mod_path: path to calling module
        param: max_mem: maximum memory to use for this plugin
        """

        self.queue_name = queue_name
        self.batch_name = batch_name
        self.ext_class = extractor_class
        self.conf = Configuration()
        self.mod_path = mod_path
        self.max_mem = max_mem

        # Managing log directory
        log_path = mod_path + "../log/"
        if not os.path.exists(log_path):
            os.makedirs(log_path)
            logging.warn("Created log directory [%s]" % log_path)

        # Instanciating Logger
        logging.basicConfig(level=logging.WARNING,
                            format='[%(levelname)s][%(name)s][%(asctime)s] %(message)s')
        logging.info("This program runs under [%d] PID - use SIGINT (9) to kill it" % os.getpid())
        logging.info("kill -9 %d" % os.getpid())

    def q(self, proc):
        q = WebsiteQueue(queue_name=self.queue_name, queue_size=proc, extractor=self.ext_class,
                         batch_name=self.batch_name, conf=self.conf, max_mem=self.max_mem)
        q.run()

    def compute_from_queue(self, proc=1):
        """
        param: proc: amount of processes to use to compute data from queue
        """
        self.q(proc=proc)
        logging.info("Program is OVER.")

        return

    def launch(self):
        proc = conf = None

        try:
            opts, args = getopt.getopt(sys.argv[1:], "", ["help", "proc=", "conf="])
        except getopt.GetoptError:
            # print help information and exit:
            print("unknown option used")
            self.usage()
            sys.exit(2)

        for o, a in opts:
            if o == "--proc":
                proc = int(a)

            if o == "--conf":
                conf = a

            elif o == "--help":
                self.usage()
                return

        # Now listen sigalarm to print stack if asked
        self.listen()

        # Loading configuration file
        if conf is None:
            print("Path to configuration file is obligatory")
            self.usage()
            sys.exit(1)

        self.conf.load(conf)

        if proc is not None:
            self.compute_from_queue(proc)
        else:
            self.compute_from_queue()

    def usage(self):
        print("*** Greetings traveller, and welcome to FCUM ***")
        print("          - FOCUS CRAWLER USER MANUAL -")
        print("\t--help              prints help")
        print("--- Queue mode ---")
        print("\t--proc              Amount of processes to use when computing data.")
        print("\t--conf              Path to configuration file.")

    def debug(self, sig, frame):
        """
        Method used to print stack on a sigalarm of current process and
        all sons.
        """
        print("*** STACKTRACE ***", file=sys.stderr)
        for threadId, stack in sys._current_frames().items():
            print("# ThreadID %s" % threadId, file=sys.stderr)
            for filename, lineno, name, line in traceback.extract_stack(stack):
                print('  File: "%s", line %d, in %s' % (filename, lineno, name),
                      file=sys.stderr)
                if line:
                    print('        %s' % (line.strip()), file=sys.stderr)
                    print(file=sys.stderr)
                    print(file=sys.stderr)
                    sys.stdout.flush()
                    sys.stderr.flush()

    def listen(self):
        signal.signal(signal.SIGUSR1, self.debug)    # Register handler


class Extractor:
    """
    Generic extractor
    """
    def __init__(self, batch_name, wanted_fields, conf, batch_size=None):
        # TODO: why wanted_fields still exists here ?
        self.batch_name = batch_name
        self.conf = conf
        if batch_size is not None and batch_size > 1:
            self.batch_size = batch_size
        else:
            # force value to 1, doing so we avoid user send a batch_size=0
            self.batch_size = 1

    def extract_with_connection(self, connection, *args, **kwargs):
        return self.extract(*args, **kwargs)

    def extract_multiple_with_connection(self, connection, *args, **kwargs):
        return self.extract_multiple(*args, **kwargs)

    def extract(self, headers, properties, message):
        raise NotImplementedError("Calling extract method while this has not been overridden")

    def extract_multiple(self, messages):
        raise NotImplementedError("Calling extract_multiple method while this has not been overridden")

if __name__ == "__main__":
    from companies import LIB_PATH
    # batch_name, queue_name, wanted_fields, extractor_class,
    m = Main("test", "test", Extractor, mod_path=LIB_PATH)
    m.launch()
