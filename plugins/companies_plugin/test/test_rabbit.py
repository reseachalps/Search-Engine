import sys
import os.path
sys.path = [os.path.abspath("../companies_plugin"), os.path.abspath("..")] + sys.path

from unittest.mock import patch
from nose.tools import assert_equal
import json

from companies_plugin.utils import QueueListener
from rabbitpy.exceptions import RabbitpyException


class QueueListenerTester(QueueListener):
    def __init__(self, stop_after=3, raise_at=None, connection_max_retries=1):
        super().__init__({}, "TEST", connection_max_retries=connection_max_retries)
        self.messages = []
        self.stop_after = 3
        self.raise_at = raise_at

    def process_message(self, message):
        self.messages.append(message)

        if self.raise_at is not None and self.raise_at == self.stop_after:
            self.raise_at = None
            self.stop_after -= 1
            raise Exception("Pouet")

        # Auto-stop the queue after some messages are processed
        self.stop_after -= 1

        if self.stop_after <= 0:
            self.stop()

        return message["a"]


class FakeInputMessage:
    # raise_error : will throw a rabbit exception when
    # trying to ack / nack
    # since the driver will handle this as a disconnection,
    # it will be retried, but next time we will not raise
    # the exception again
    def __init__(self, body, raise_error=False):
        self.body = json.dumps(body).encode()
        self.properties = {
            "reply_to": b"pouet",
            "priority": 1
        }
        self.ack_status = None
        self.raise_error = raise_error

    def ack(self):
        if self.raise_error:
            self.raise_error = False
            raise RabbitpyException()
        self.ack_status = True

    def nack(self):
        if self.raise_error:
            self.raise_error = False
            raise RabbitpyException()
        self.ack_status = False


@patch("rabbitpy.Connection", autospec=True)
@patch("rabbitpy.Queue", autospec=True)
@patch("rabbitpy.Message", autospec=True)
@patch("time.sleep")
class Tester:
    def test_receive_message(self, sleep, Message, Queue, Connection):
        messages = [{"a": 1}, {"a": 2}, {"a": 3}]
        input_messages = [FakeInputMessage(m) for m in messages]

        Queue.return_value.consume.return_value = input_messages

        #
        q = QueueListenerTester(len(messages))
        q.run()

        assert_equal(q.messages, messages)
        for m in input_messages:
            assert m.ack_status

        for (expected, real) in zip(messages, Message.call_args_list):
            assert_equal(expected["a"], real[0][1])

    def test_connection_error(self, sleep, Message, Queue, Connection):
        messages = [{"a": 1}, {"a": 2}, {"a": 3}]
        input_messages = [FakeInputMessage(m) for m in messages]
        input_messages[1].raise_error = True

        # iter so that when we will raise an error, we continue to the next available message
        # instead of generating again 3 new messages
        Queue.return_value.consume.return_value = iter(input_messages)

        q = QueueListenerTester(len(messages), connection_max_retries=2)
        q.run()

        assert_equal(q.messages, messages)

        # We do not requeue the failed message, so it should not be acked nor nacked
        for m in zip(input_messages, [True, None, True]):
            assert_equal(m[0].ack_status, m[1])

        for (expected, real) in zip(messages, Message.call_args_list):
            assert_equal(expected["a"], real[0][1])

    def test_processing_error(self, sleep, Message, Queue, Connection):
        messages = [{"a": 1}, {"a": 2}, {"a": 3}]
        input_messages = [FakeInputMessage(m) for m in messages]

        Queue.return_value.consume.return_value = iter(input_messages)

        #
        q = QueueListenerTester(len(messages), raise_at=2)
        q.run()

        assert_equal(q.messages, messages)
        for m in input_messages:
            assert m.ack_status

        assert_equal(messages[0]["a"], Message.call_args_list[0][0][1])
        assert_equal(messages[2]["a"], Message.call_args_list[2][0][1])
        error_msg = Message.call_args_list[1][0][1]
        assert type(error_msg) is dict
        assert "error" in error_msg and len(error_msg["error"]) > 0
        assert "original_message" in error_msg and error_msg["original_message"] == json.dumps(messages[1])
