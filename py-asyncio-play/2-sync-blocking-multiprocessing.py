
import socket
import time
from concurrent import futures

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

    with futures.ProcessPoolExecutor(10) as executor:
        fs = {executor.submit(blocking_way) for i in range(10)}
    for f in fs:
        f.result()

    # Output: 2.085, overhead of process context switch
    print(time.time() - start)