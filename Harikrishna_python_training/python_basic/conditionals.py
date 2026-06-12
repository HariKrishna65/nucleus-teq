"""Conditional Statements"""


def even_or_odd(number: int) -> str:
    return "Even" if number % 2 == 0 else "Odd"


def positive_negative_zero(number: int) -> str:
    if number > 0:
        return "Positive"

    if number < 0:
        return "Negative"

    return "Zero"


def largest_of_three(
    first_number: int,
    second_number: int,
    third_number: int
) -> int:

    return max(
        first_number,
        second_number,
        third_number
    )


def calculate_grade(marks: int) -> str:

    if marks >= 90:
        return "A"

    if marks >= 75:
        return "B"

    if marks >= 50:
        return "C"

    return "Fail"


def is_leap_year(year: int) -> bool:

    return (
        year % 4 == 0 and
        (year % 100 != 0 or year % 400 == 0)
    )