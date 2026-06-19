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
