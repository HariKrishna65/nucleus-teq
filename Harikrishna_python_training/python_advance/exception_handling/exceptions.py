def validate_integer(value: str) -> int:
    try:
        return int(value)
    except ValueError:
        print("Invalid Integer")
        raise


def divide_numbers(first_number: float, second_number: float) -> float:
    try:
        return first_number / second_number
    except ZeroDivisionError:
        print("Cannot divide by zero")
        raise


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


def handle_multiple_exceptions(
    first_number: int,
    second_number: int
) -> float:

    try:
        return first_number / second_number

    except (ValueError, ZeroDivisionError) as error:
        print(error)
        raise


def validate_positive(number: int) -> int:

    if number < 0:
        raise ValueError(
            "Negative numbers not allowed"
        )

    return number


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