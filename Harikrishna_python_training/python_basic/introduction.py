"""Introduction to Python"""

import sys


def welcome_message() -> None:
    print("Welcome to Python Training")


def display_python_version() -> None:
    print(sys.version)


def user_information(name: str, age: int) -> str:
    return f"My name is {name} and I am {age} years old."


def display_data_types() -> None:
    integer_value = 10
    float_value = 10.5
    string_value = "Python"
    boolean_value = True

    print(type(integer_value))
    print(type(float_value))
    print(type(string_value))
    print(type(boolean_value))


def swap_numbers(first_number: int, second_number: int) -> tuple[int, int]:
    return second_number, first_number


def arithmetic_operations(
    first_number: float,
    second_number: float
) -> dict[str, float]:

    return {
        "sum": first_number + second_number,
        "difference": first_number - second_number,
        "multiplication": first_number * second_number,
        "division": first_number / second_number
    }