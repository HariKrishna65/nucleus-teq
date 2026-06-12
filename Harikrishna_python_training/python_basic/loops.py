"""Loop Programs"""


def numbers_one_to_hundred() -> list[int]:
    return list(range(1, 101))


def multiplication_table(number: int) -> list[str]:

    return [
        f"{number} x {value} = {number * value}"
        for value in range(1, 11)
    ]


def factorial(number: int) -> int:

    result = 1

    for value in range(1, number + 1):
        result *= value

    return result


def reverse_number(number: int) -> int:

    reversed_number = 0

    while number > 0:
        digit = number % 10
        reversed_number = reversed_number * 10 + digit
        number //= 10

    return reversed_number


def is_prime(number: int) -> bool:

    if number <= 1:
        return False

    for value in range(
        2,
        int(number ** 0.5) + 1
    ):
        if number % value == 0:
            return False

    return True