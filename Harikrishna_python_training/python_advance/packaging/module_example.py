"""
Create a module with two utility functions and import it into another Python
file.
"""
from python_advance.packaging.utilities import (
    greet,
    square
)

print(greet("Hari"))
print(square(5))


"""
Explain the difference between a module and a package with an example.
"""
def module_package_difference():

    return (
        "A module is a single Python file, such as utilities.py. "
        "A package is a folder containing modules and an __init__.py file, "
        "such as math_package."
    )
