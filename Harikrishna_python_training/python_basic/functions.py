"""Function Examples"""

from python_basic.constants import DEFAULT_GREETING


def square(number: int) -> int:
    return number * number


def palindrome(value: str) -> bool:
    return value == value[::-1]


def maximum_number(numbers: list[int]) -> int:
    return max(numbers)


def greet(
    name: str = DEFAULT_GREETING
) -> str:

    return f"Hello {name}"