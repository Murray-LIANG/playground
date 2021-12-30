
import socket
import selectors
import time

selector = selectors.DefaultSelector()

url_todo = {'/', '/1', '/2', '/3', '/4', '/5', '/6', '/7', '/8', '/9'}
stopped = False

class Crawler:
    def __init__(self, url):
        self.url = url
        # Point #4: self.sock is shared in different callbacks.
        self.sock = None
        self.response = b''

    def fetch(self):
        self.sock = socket.socket()
        self.sock.setblocking(False)

        try:
            self.sock.connect(('example.com', 80))
        except BlockingIOError:
            pass

        selector.register(self.sock.fileno(), selectors.EVENT_WRITE, self.send)

    # Point #3: callback func.
    def send(self, key, mask):
        selector.unregister(key.fd)
        get = 'GET {} HTTP/1.0\r\nHost: example.com\r\n\r\n'.format(self.url)
        self.sock.send(get.encode('ascii'))
        
        # Point #5: callback nested in another callback.
        selector.register(key.fd, selectors.EVENT_READ, self.receive)

    # Callback func.
    def receive(self, key, mask):

        chunk = self.sock.recv(4096)
        if chunk:
            self.response += chunk
        else:
            # All response data is read.
            selector.unregister(key.fd)

            url_todo.remove(self.url)
            if not url_todo:
                global stopped
                stopped = True


def eventloop():
    # Point #2: event loop.
    while not stopped:
        events = selector.select()  # blocking until any event happens
        for event_key, event_mask in events:
            callback = event_key.data
            callback(event_key, event_mask)

        
if __name__ == "__main__":
    # Point #1: concurrent in one thread, no context switch and .etc.
    start = time.time()
    for url in url_todo:
        crawler = Crawler(url)
        crawler.fetch()
    
    eventloop()
    
    print(time.time() - start)