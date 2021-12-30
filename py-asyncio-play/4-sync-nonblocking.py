
import socket
import time

def nonblocking_way():

    sock = socket.socket()
    sock.setblocking(False)

    try:
        sock.connect(('example.com', 80))
    except BlockingIOError:
        # It's OK because we'll wait for socket ready before send/recv.
        pass
    
    request = 'GET / HTTP/1.0\r\nHost: example.com\r\n\r\n'
    data = request.encode('ascii')
    
    # Although send is nonblocking, but the while loop is waiting for socket ready, which
    # means blocking.
    while True:
        try:
            sock.send(data)
            break  # send successfully
        except OSError:
            # socket not ready for send yet.
            pass

    response = b''
    while True:
        try:
            chunk = sock.recv(4096)
            while chunk:
                response += chunk
                chunk = sock.recv(4096)
            break  # receive successfully
        except OSError:
            # socket not ready to read yet.
            pass

    return response


if __name__ == "__main__":
    start = time.time()

    res = []
    # 10 downloading are executed one by one (synchronously).
    for i in range(10):
        res.append(nonblocking_way())
    
    # Output: 4.119, the same as blocking way
    print(time.time() - start)