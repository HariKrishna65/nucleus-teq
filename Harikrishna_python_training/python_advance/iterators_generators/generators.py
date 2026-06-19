"""
Create an iterator for a list and print elements using next().
"""
def print_list_with_next(values: list):

    iterator = iter(values)

    try:
        while True:
            print(next(iterator))
    except StopIteration:
        pass


"""
Write a custom iterator class that returns numbers from 1 to N.
"""
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


"""
Write a generator function that yields square numbers up to N.
"""
def square_generator(limit: int):

    for number in range(1, limit + 1):
        yield number * number


"""
Write a generator to produce Fibonacci numbers.
"""
def fibonacci_generator(limit: int):

    first = 0
    second = 1

    for _ in range(limit):
        yield first
        first, second = second, first + second


"""
Write a generator expression to generate even numbers from 1 to 50.
"""
def even_generator():

    return (
        number
        for number in range(1, 51)
        if number % 2 == 0
    )


"""
Explain the difference between iterator and generator with a small example.
"""
def iterator_generator_difference():

    numbers_iterator = iter([1, 2, 3])

    def simple_generator():
        yield 1
        yield 2
        yield 3

    explanation = (
        "An iterator is any object with __iter__() and __next__(). "
        "A generator is a simpler way to create an iterator using yield."
    )

    return explanation, next(numbers_iterator), next(simple_generator())


"""
Write a program that processes a large dataset using a generator instead of
storing all values in a list.
"""
def large_dataset():

    for value in range(1_000_000):
        yield value


def sum_large_dataset():

    total = 0

    for value in large_dataset():
        total += value

    return total


"""
Show an example of a built-in generator (like range) and iterate over it.
"""
def iterate_range():

    for number in range(1, 6):
        print(number)
