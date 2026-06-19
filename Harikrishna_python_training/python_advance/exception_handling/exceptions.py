"""
Write a program that takes a number as input and handles ValueError if the
input is not a valid integer.
"""
def validate_integer(value: str) -> int:
    try:
        return int(value)
    except ValueError:
        print("Invalid Integer")
        raise


"""
Write a program to divide two numbers entered by the user and handle
ZeroDivisionError.
"""
def divide_numbers(first_number: float, second_number: float) -> float:
    try:
        return first_number / second_number
    except ZeroDivisionError:
        print("Cannot divide by zero")
        raise


"""
Write a program using try-except-else-finally to read a number from a file
and print its square.
"""
def read_square(file_name: str) -> int:
    try:
        with open(file_name, "r", encoding="utf-8") as file:
            number = int(file.read())

    except Exception as error:
        print(error)
        raise

    else:
        return number * number

    finally:
        print("Execution Completed")


"""
Handle multiple exceptions in a single program.
"""
def handle_multiple_exceptions(
    first_number: int,
    second_number: int
) -> float:

    try:
        return first_number / second_number

    except (ValueError, ZeroDivisionError) as error:
        print(error)
        raise


"""
Write a program that catches all exceptions and prints the error message.
"""
def catch_all_exceptions(value: str):

    try:
        number = int(value)
        return 100 / number

    except Exception as error:
        print(f"Error: {error}")
        return None


"""
Create a function that raises a ValueError if a number is negative.
"""
def validate_positive(number: int) -> int:

    if number < 0:
        raise ValueError(
            "Negative numbers not allowed"
        )

    return number


"""
Create a custom exception called AgeException and raise it if age is less
than 18.
"""
class AgeException(Exception):
    """Raised when age is less than 18."""


def validate_age(age: int) -> str:

    if age < 18:
        raise AgeException("Age must be at least 18")

    return "Eligible"


"""
Write a program that handles FileNotFoundError when trying to open a file.
"""
def open_file(file_name: str) -> str:

    try:
        with open(
            file_name,
            "r",
            encoding="utf-8"
        ) as file:
            return file.read()

    except FileNotFoundError:
        return "File Not Found"
