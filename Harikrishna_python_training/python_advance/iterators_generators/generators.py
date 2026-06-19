class NumberIterator:

    def __init__(self, limit: int):
        self.limit = limit
        self.current = 1

    def __iter__(self):
        return self

    def __next__(self):

        if self.current > self.limit:
            raise StopIteration

        value = self.current
        self.current += 1

        return value


def square_generator(limit: int):

    for number in range(1, limit + 1):
        yield number * number


def fibonacci_generator(limit: int):

    first = 0
    second = 1

    for _ in range(limit):
        yield first
        first, second = second, first + second


def even_generator():

    return (
        number
        for number in range(1, 51)
        if number % 2 == 0
    )


def large_dataset():

    for value in range(1_000_000):
        yield value