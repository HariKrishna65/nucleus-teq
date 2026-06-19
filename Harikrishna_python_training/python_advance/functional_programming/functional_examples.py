from functools import reduce


square = lambda number: number * number


def map_squares(numbers: list[int]):

    return list(
        map(
            lambda number: number * number,
            numbers
        )
    )


def filter_even(numbers: list[int]):

    return list(
        filter(
            lambda number: number % 2 == 0,
            numbers
        )
    )


def product(numbers: list[int]):

    return reduce(
        lambda first, second:
        first * second,
        numbers
    )


def factorial(number: int):

    if number <= 1:
        return 1

    return number * factorial(number - 1)


def fibonacci(number: int):

    if number <= 1:
        return number

    return (
        fibonacci(number - 1)
        + fibonacci(number - 2)
    )