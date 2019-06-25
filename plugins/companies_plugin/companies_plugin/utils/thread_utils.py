import ctypes


def setthreadname(name):  # pragma: no cover # we don't need tests for an esthetic feature
    """
    Set a thread name.
    The name of a thread cannot have more than 15 characters (bytes).

    >>> from threading import Thread
    >>> def set_my_name():
    ...     import time
    ...     setthreadname("pouet")
    ...     print("use htop to see my name !")
    ...     time.sleep(1000)
    >>> # Set daemon=True to avoid blocking doctest
    >>> Thread(target=set_my_name, daemon=True).start()

    You can display thread names in htop by doing :
    [F2]
    > Display Options
    [x] Show custom thread names

    You can also tick
    [x] display Threads in a different color
    """
    try:
        libpthread = ctypes.cdll.LoadLibrary('libpthread.so.0')
        pthread_self = libpthread.pthread_self
        pthread_self.restype = ctypes.c_ulong
        tid = pthread_self()
        pthread_setname_np = libpthread.pthread_setname_np
        pthread_setname_np.argtypes = [ctypes.c_ulong, ctypes.c_char_p]
        libpthread.pthread_setname_np(tid, name.encode()[:15])
    except:
        pass


# SOME UGLY MONKEY PATCHES TO HAVE NICE THREAD NAMES !


def patch_threadpools():  # pragma: no cover #
    """
    Some ugly monkey patches to have nice thread names for python thread pools.
    Set thread names for :
        - multiprocessing.pool.Pool (used to manage Processes)
        - concurrent.futures.ThreadPoolExecutor (used by worker and to manage them)

    You must call this function only once, and before using one of these thread pools.

    >>> patch_threadpools()
    >>> from concurrent.futures import ThreadPoolExecutor
    >>> with ThreadPoolExecutor(1) as tpe:
    ...     _ = tpe.map(print, [1, 2, 3])
    1
    2
    3

    See `setthreadname` to learn more about that.
    """
    from multiprocessing.pool import Pool
    from concurrent.futures import thread
    from concurrent.futures import ThreadPoolExecutor

    # Patch multiprocessing.pool internal thread pool
    for method in ["workers", "tasks", "results"]:
        handle_method = "_handle_" + method
        old_method = getattr(Pool, handle_method)

        def wrapper(method, handle_method, old_method):
            def _handle(*args, **kwargs):
                setthreadname("MP %s" % method.capitalize())
                return old_method(*args, **kwargs)
            return _handle

        setattr(Pool, handle_method, wrapper(method, handle_method, old_method))

    # Patch concurrent.futures thread pool executor
    old_worker = thread._worker

    def worker(*args):
        setthreadname("TP executor")
        return old_worker(*args)

    setattr(thread, "_worker", worker)

    old_submit = ThreadPoolExecutor.submit

    def new_submit(self, fn, *args, **kwargs):
        def new_fn(*args, **kwargs):
            setthreadname("[T] %s" % fn.__name__)
            return fn(*args, **kwargs)

        return old_submit(self, new_fn, *args, **kwargs)

    ThreadPoolExecutor.submit = new_submit
