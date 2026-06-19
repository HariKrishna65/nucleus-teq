from functools import reduce


"""
Write a lambda function to find the square of a number.
"""
square = lambda number: number * number


"""
Use map() to convert a list of numbers into their squares.
"""
def map_squares(numbers: list[int]):

    return list(
        map(
            lambda number: number * number,
            numbers
        )
    )


"""
Use filter() to extract even numbers from a list.
"""
def filter_even(numbers: list[int]):

    return list(
        filter(
            lambda number: number % 2 == 0,
            numbers
        )
    )


"""
Use reduce() to find the product of all elements in a list.
"""
def product(numbers: list[int]):

    return reduce(
        lambda first, second:
        first * second,
        numbers
    )


"""
Write a recursive function to calculate factorial.
"""
def factorial(number: int):

    if number <= 1:
        return 1

    return number * factorial(number - 1)


"""
Write a recursive function to calculate Fibonacci.
"""
def fibonacci(number: int):

    if number <= 1:
        return number

    return (
        fibonacci(number - 1)
        + fibonacci(number - 2)
    )


"""
Convert a simple loop-based program into a functional style using map or
filter.
"""
def convert_loop_to_functional(numbers: list[int]):

    return list(
        map(
            lambda number: number * 2,
            filter(
                lambda number: number % 2 == 0,
                numbers
            )
        )
    )
