# Monkey patch!
from kombu.messaging import Consumer
Consumer.auto_declare = False
# This is a place for crazy people. I'm not crazy.

from multiprocessing import Process
from queue           import Empty
from kombu           import Connection
from amqp.exceptions import ConsumerCancelled
from collections     import namedtuple
import logging
import traceback
import os
from companies_plugin import sandbox

Message = namedtuple("Message", ["headers", "body", "properties"])


class Subscription(Process):
    def __init__(self, queue_name, extractor, batch_name, conf, prefetch_count=1, max_mem=None):
        Process.__init__(self)
        self._queue_name = queue_name
        self._batch_name = batch_name
        self._conf = conf

        self._extractor = extractor

        self._hostname = conf.rabbit["hostname"]
        self._username = conf.rabbit["username"]
        self._password = conf.rabbit["password"]

        self._prefetch_count = prefetch_count
        self._stopped = False

        self._max_mem = max_mem or conf.plugin.get("max_mem")

    def run(self):
        self._extractor = self._extractor(self._batch_name, [], self._conf)
        while True:
            try:
                with Connection("amqp://" + self._username + ":" + self._password + "@" + self._hostname + "/") as conn:
                    with conn.channel() as channel:
                        channel.basic_qos(0, self._prefetch_count, False)
                        queue = conn.SimpleQueue(self._queue_name, channel=channel, no_ack=False)
                        self.process_message(queue, conn)

                        if self._stopped:
                                return

                        logging.error("Connection lost, reconnecting... [PID %s]" % (os.getpid()))

            # Theses exceptions need no import in python 3
            except (ConsumerCancelled, ConnectionResetError, BrokenPipeError, OSError):
                continue
            except:
                logging.error("When disconnecting, got exception:")
                logging.error(''.join(traceback.format_exc()))
                raise

    def process_message(self, queue, conn):
        # While listener is not stopped
        while not self._stopped:
            _batch_size = self._extractor.batch_size
            messages = []
            while _batch_size > 0:
                try:
                    # Wait for the next message (1 sec, to stop properly)
                    messages.append(queue.get(timeout=1))
                    _batch_size = _batch_size - 1 if queue.qsize() > 0 else 0
                except (ConsumerCancelled, ConnectionResetError, BrokenPipeError, OSError):
                    # Reconnect
                    return
                except Empty:
                    continue
                except:
                    logging.error(''.join(traceback.format_exc()))
                    for message in messages:
                        message.requeue()
                    raise

            msgs = [Message(message.headers, message.body.decode(), message.properties) for message in messages]
            try:
                if self._extractor.batch_size == 1:
                    _message = msgs[0]
                    # Get reply queue name and response document
                    cjson, reply = sandbox.exec(self._extractor.extract_with_connection, self._max_mem, None, conn,
                                                _message.headers, _message.properties, _message.body)
                    responses = [(cjson, reply)]
                else:
                    responses = sandbox.exec(self._extractor.extract_multiple_with_connection, self._max_mem, None,
                                             conn, msgs)

                # Send responses
                for message, response in zip(messages, responses):
                    # Get the priority (None if not supplied)
                    priority = message.properties.get("priority")
                    cjson, reply = response
                    # Send response
                    with conn.Producer(routing_key=reply) as producer:
                        producer.publish(cjson, headers={"source-id": self._queue_name}, priority=priority)
                    # Send ack
                    message.ack()
            # Network/Rabbit issue
            except (ConsumerCancelled, ConnectionResetError, BrokenPipeError, OSError):
                # Reconnect
                return
            # Processing issue
            except:
                # Send all unack message
                for message in messages:
                    if not message.acknowledged:
                        msg = message.body.decode()
                        self.forward_error(message, msg, conn, traceback.format_exc())

    def forward_error(self, message, msg, conn, stack):
        """
        When an unexpected error occurred while processing a message,
        it is forwarded to a PLUGIN_ERROR queue.
        """
        newmsg = {"error": stack, "original_message": msg,
                  "queue": self._queue_name, "reply_to": message.properties["reply_to"]}
        # Get reply queue name and get the queue reference id not in index
        try:
            # Forwards message
            with conn.Producer(routing_key="PLUGIN_ERROR") as producer:
                producer.publish(newmsg, headers={"source-id": self._queue_name}, priority=message.properties.get("priority"))

            # Send ack
            message.ack()
            logging.warning("An error occurred and has been forwarded to PLUGIN_ERROR queue : \n%s" % stack)

        except (ConsumerCancelled, ConnectionResetError, BrokenPipeError, OSError):
            # Reconnect
            return

        except:
            logging.error(''.join(traceback.format_exc()))
            message.requeue()
            raise

    def stop(self):
        self._stopped = True


class WebsiteQueue:
    """
    Listen to a queue on network to fill a local queue available to plugin.
    """

    def __init__(self, queue_name, queue_size, extractor, batch_name, conf, max_mem=None):
        """
        param: conn: Connection with father process
        """
        self.batch_name = batch_name
        self.conf = conf
        self.queue_name = queue_name
        self.queue_size = queue_size
        self.maxlen = queue_size
        self.minlen = 0
        self.processes = {}
        self.extractor = extractor
        self.stopped = False
        self.max_mem = max_mem

        # Instanciating sublogger
        self.logger = logging.getLogger("company_queue")
        self.logger.setLevel(logging.INFO)

    def run(self):
        # Filling queue
        missing = self.maxlen - len(self.processes)
        if missing > 0:
            for i in range(0, missing):
                sub = Subscription(queue_name=self.queue_name,
                                   extractor=self.extractor,
                                   batch_name=self.batch_name,
                                   conf=self.conf,
                                   max_mem=self.max_mem)
                sub.start()
                self.processes[i] = sub
                self.logger.info("launching [%d]" % i)

        # Now waiting for subscriptions to die (should not happen)
        for i in self.processes:
            self.processes[i].join()

        self.logger.warning("Ended all of %d processes." % len(self.processes))

# For testing purpose
if __name__ == "__main__":
    pass
