import socket
import selectors
import time

selector = selectors.DefaultSelector()

url_todo = {'/', '/1', '/2', '/3', '/4', '/5', '/6', '/7', '/8', '/9'}
stopped = False


class Future:
    def __init__(self):
        self.result = None
        # Point #6: these callbacks are not for business logic.
        self._callbacks = []
    
    def add_done_callback(self, fn):
        self._callbacks.append(fn)

    def set_result(self, result):
        self.result = result
        for fn in self._callbacks:
            fn(self)

    def __iter__(self):
        # Point #8: make Future iterable, which can be used in `yield from`.
        yield self
        return self.result

        
def connect(sock, address):
    sock.setblocking(False)

    try:
        sock.connect(address)
    except BlockingIOError:
        pass

    f = Future()

    # Point #3: callback func.
    def on_connected():
        f.set_result(None)

    selector.register(sock.fileno(), selectors.EVENT_WRITE, on_connected)
    yield from f

    selector.unregister(sock.fileno())

    
def read(sock):
    f = Future()

    def on_readable():
        f.set_result(sock.recv(4096))

    # Point #5: no more callback nested in another callback.
    selector.register(sock.fileno(), selectors.EVENT_READ, on_readable)

    chunk = yield from f
    selector.unregister(sock.fileno())
    return chunk


def read_all(sock):
    response = []
    chunk = yield from read(sock)
    while chunk:
        response.append(chunk)
        chunk = yield from read(sock)
    return b''.join(response)


class Crawler:
    def __init__(self, url):
        self.url = url
        # Point #4: no more shared self.sock

        self.response = b''

    def fetch(self):
        sock = socket.socket()

        yield from connect(sock)

        get = 'GET {} HTTP/1.0\r\nHost: example.com\r\n\r\n'.format(self.url)
        sock.send(get.encode('ascii'))
        
        self.response = yield from read_all(sock)
        
        url_todo.remove(self.url)
        if not url_todo:
            global stopped
            stopped = True


# Point #9: together with future's done_callback, drive running of the coroutine.
class Task:
    def __init__(self, coro):
        self.coro = coro
        f = Future()
        f.set_result(None)
        self.step(f)

    def step(self, future):
        try:
            next_future = self.coro.send(future.result)
        except StopIteration:
            return
        next_future.add_done_callback(self.step)


def eventloop():
    # Point #2: event loop.
    while not stopped:
        events = selector.select()  # blocking until any event happens
        for event_key, _ in events:
            callback = event_key.data
            # Point #7: no need to pass in event. callback doesn't care about the event.
            # All the things are stored in the coroutine.
            callback()

        
if __name__ == "__main__":
    # Point #1: concurrent in one thread, no context switch and .etc.
    start = time.time()
    for url in url_todo:
        crawler = Crawler(url)
        Task(crawler.fetch())
    
    eventloop()
    
    print(time.time() - start)