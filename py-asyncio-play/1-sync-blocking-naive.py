import socket
import time


def blocking_way():
    sock = socket.socket()

    sock.connect(('example.com', 80))  # blocking
    request = 'GET / HTTP/1.0\r\nHost: example.com\r\n\r\n'
    sock.send(request.encode('ascii'))  # blocking

    response = b''
    chunk = sock.recv(4096)  # blocking
    while chunk:
        response += chunk
        chunk = sock.recv(4096)  # blocking

    return response


if __name__ == "__main__":
    start = time.time()
    res = []
    for i in range(10):
        res.append(blocking_way())

    # Output: 4.426
    print(time.time() - start)
