import resource
import signal


class FakeTraceback:
    pass


class ExceptionWithFrame(Exception):
    def __init__(self):
        self.tb = None

    @property
    def __traceback__(self):
        return self.tb

    @__traceback__.setter
    def __traceback__(self, value):
        self.tb = value

    def with_traceback(self, tb):
        self.tb = FakeTraceback()
        self.tb.tb_frame = tb
        self.tb.tb_lasti = 0
        self.tb.tb_lineno = 0
        self.tb.tb_next = None
        return self


class TooMuchCPUTime(ExceptionWithFrame):
    pass


class TooMuchMemory(ExceptionWithFrame):
    pass


def exec(fun, memory=None, cpu_time=None, *args, **kwargs):
    """
    Execute the given function with *args and **kwargs.
    If the global memory (before calling the function + inside the function)
      exceeds `memory`, this raises TooMuchMemory.
    If the global CPU usage (before calling the function + inside the function)
      exceeds `cpu_time`, this raises TooMuchCPUTime
    """

    def cpu_handler(signum, stack):
        raise TooMuchCPUTime().with_traceback(stack)

    old_cpu_handler = signal.signal(signal.SIGXCPU, cpu_handler)

    memory_limits = [resource.RLIMIT_DATA, resource.RLIMIT_AS, resource.RLIMIT_RSS, resource.RLIMIT_STACK]
    memory_limits = {k: resource.getrlimit(k) for k in memory_limits}

    if memory is not None:
        for limit in memory_limits.keys():
            resource.setrlimit(limit, (memory, memory_limits[limit][1]))

    cpu_limit = resource.getrlimit(resource.RLIMIT_CPU)
    if cpu_time is not None:
        resource.setrlimit(resource.RLIMIT_CPU, (cpu_time, cpu_limit[1]))

    def reset():
        signal.signal(signal.SIGXCPU, old_cpu_handler)
        resource.setrlimit(resource.RLIMIT_CPU, cpu_limit)
        for limit in memory_limits.keys():
            resource.setrlimit(limit, memory_limits[limit])

    try:
        try:
            ret = fun(*args, **kwargs)
            return ret
        except MemoryError:
            reset()
            raise TooMuchMemory()
    finally:
        reset()
