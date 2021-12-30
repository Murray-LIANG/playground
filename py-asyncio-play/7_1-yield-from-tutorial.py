
def gen_yield_from():
    subgen = range(10)
    # Save the for loop.
    yield from subgen

def gen_yield_in_for_loop():
    subgen = range(10)
    for item in subgen:
        yield item

def gen():
    # Bi-direction tunnel from/to subgen.
    yield from subgen()

def subgen():
    while True:
        x = yield  # suspended by next(g), then x = 1 by g.send(1)
        yield x + 1  # suspended and return 2, then exception by g.throw

def main():
    # All the operations - next, send, and throw, take on subgen.
    g = gen()
    next(g)
    retval = g.send(1)
    print(retval)  # output: 2
    g.throw(StopIteration)


if __name__ == "__main__":
    main()
