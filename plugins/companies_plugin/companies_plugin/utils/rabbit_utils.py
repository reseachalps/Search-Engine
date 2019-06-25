import rabbitpy
import json
from companies_plugin.utils import add_logger, get_stack
from time import sleep
from rabbitpy.exceptions import RabbitpyException


@add_logger
class QueueListener:
    """
    A queue listener backed by rabbitpy.

    >>> class MyQueueListener(QueueListener):
    ...     def __init__(self, conf, any_attribute):
    ...         super().__init__(conf, "my_queue")
    ...         self.my_attribute = any_attribute
    ...
    ...     def process_message(self, message):
    ...         value = message.get("some_value")
    ...         # If a value is returned, it will be sent to the queue specified in the "reply_to" attribute of the original message
    ...         return {"success": True, "new_value": value + self.my_attribute}
    ...
    ...     def on_error(self, message, exception):
    ...         return {"success": False, "error": str(exception)}
    ...
    >>> conf = {"hostname": "localhost"}
    >>> listener = MyQueueListener(conf, 123)
    >>> listener.run()  # doctest: +SKIP

    When receiving a message, process_message will be called.
    If `process_message` returns a value, this will be sent to the reply queue specified in the original message properties.

    If there is an exception in `process_message`, `on_error` will be called with the message and the exception.
    If the `on_error` returns a value, this value will be sent to the reply queue.

    If `on_error` raises an exception, a message will be sent to the "PLUGIN_ERROR" queue.

    In all cases, the message is ack'ed.

    If there was an error when communicating with rabbit, then the message is unack'ed.

    This class is not threaded, it will block when calling `run`().
    It expects the messages to be in JSON.

    """
    def __init__(self, conf, queue_name, prefetch=100, connection_max_retries=3):
        """
        Creates the queue listener.
        :param conf: a configuration dictionnary with hostname, port, username and password for rabbit
        :param queue: the name of the queue to listen to
        :param prefetch: number of messages to fetch from rabbit. Increase it if you expect a lot of small messages.
        Set it to 1 if you expect few urgent messages.
        :param connection_max_retries: if the connection fails, retry to connect to rabbit this number of times. Before each retry,
        an exponential of time of wait is performed (1, 2, 4, ...)
        """
        host, port = conf.get("hostname", "localhost"), conf.get("port", 5672)
        username, password = conf.get("username", "guest"), conf.get("password", "guest")
        self.rabbit_url = "amqp://{0}:{1}@{2}:{3}/".format(username, password, host, port)
        self.queue_name = queue_name
        self.prefetch = prefetch
        self.connection_max_retries = connection_max_retries
        self.must_stop = False

    def process_message(self, message):
        """
        Implement this method to process incoming messages.
        Any value returned (not None) will be sent to the output queue defined in the "reply_to" attribute of the original message.
        Any exception will result in `on_error` to be called.

        :param message: the incoming message
        """
        raise NotImplementedError()  # pragma: no cover

    def on_error(self, message, exception):
        """
        The error message to supply to the reply queue if there is an exception in `process_message`.
        If None is returned, nothing is sent to the reply queue.

        :param message: the incoming message
        :param exception: the exception that occured when `process_message` was called with the incoming message
        """
        raise NotImplementedError()  # pragma: no cover

    def stop(self):
        """
        Stop consuming messages.
        """
        self.must_stop = True
        if self.listening_queue is not None:
            self.listening_queue.stop_consuming()

    def run(self):
        """
        The blocking loop used to (re-)connect to rabbit.
        This function does not return, except after `self.connection_max_retries` failed connections.
        """
        connect_tries = 0  # after self.retries disconnects, just fail !
        while not self.must_stop and connect_tries < self.connection_max_retries:
            try:
                with rabbitpy.Connection(url=self.rabbit_url) as connection:
                    with connection.channel() as channel:
                        channel.enable_publisher_confirms()
                        queue = rabbitpy.Queue(channel, self.queue_name)
                        self.listening_queue = queue
                        connect_tries = 0  # connection seems ok, reset the counter
                        self._loop_with_queue(queue, channel)

                    self.listening_queue = None
            except RabbitpyException:
                self.logger.exception("Error when communicating with rabbit")
                sleep(2 ** connect_tries)
                connect_tries += 1

    def _loop_with_queue(self, queue, channel):
        """
        This method is called when the client is connected. It will iterate over incoming messages and do the logic to call
        `process_message`, `on_error`, and send messages back to rabbit.
        """
        for message in queue.consume(prefetch=self.prefetch):
            self.logger.debug("Receiving a message")
            if message is not None and message.body is not None:
                self._process_one_message(queue, channel, message)

    def _process_one_message(self, queue, channel, message):
        """Process a message received by _loop_with_queue"""
        body = None
        try:
            body = json.loads(message.body.decode())

            reply = self.process_message(body)
            if reply is not None:
                self._reply(channel, message, reply)
            message.ack()
        except RabbitpyException:
            raise
        except Exception as e:
            try:
                self.logger.exception("Error while processing a message. Trying to handle the error with on_error.")
                try:
                    self.logger.info("on_error")
                    self.on_error(body, e)
                    self.logger.info("on_error ok")
                    message.ack()
                    self.logger.info("ack ok")
                    return
                except NotImplementedError:
                    pass
            except RabbitpyException:
                raise
            except:
                self.logger.exception("Error while handling message using on_error. Pushing to the error queue")
                pass
            self._error(channel, message)

    def _reply(self, channel, src_message, content):
        """
        Replies to the queue given in `src_message` with `content`.
        """
        self.logger.debug("Replying to a message")
        reply_to = src_message.properties["reply_to"].decode()
        if self._send_message(channel, reply_to, content, src_message):
            return True
        try:  # Create a stack so that _error can use it to display a nice error
            raise Exception("Unable to send a message")
        except:
            self._error(channel, src_message)

    def _error(self, channel, message):
        """
        Insert an error in the PLUGIN_ERROR queue
        """
        try:
            self.logger.exception("Error while processing a message")
            error_message = {
                "error": get_stack(),
                "original_message": message.body.decode(),
                "queue": self.queue_name,
                "reply_to": message.properties["reply_to"].decode(),
            }

            if self._send_message(channel, "PLUGIN_ERROR", error_message, message):
                message.ack()
            else:
                message.nack()
        except RabbitpyException:
            raise
        except:
            self.logger.exception("While handling an error and trying to send the message back to the error queue, "
                                  "the following exception happened:")
            raise

    def _send_message(self, channel, queue, content, src_message):
        """
        The method used everywhere to send a message in a queue
        """
        try:
            properties = {
                "priority": src_message.properties["priority"],
                "headers": {
                    "source-id": self.queue_name
                }
            }
            reply_message = rabbitpy.Message(channel, content, properties=properties)
            # Empty exchange, routing key is the queue name
            return reply_message.publish("", queue, mandatory=True)
        except RabbitpyException:
            raise
        except:
            self.logger.exception("Unable to send a message")
            return False
