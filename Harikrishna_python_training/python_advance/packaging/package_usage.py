"""
Create a package with two modules and include an __init__.py file.
"""
from python_advance.packaging.math_package import (
    add,
    divide,
    multiply,
    subtract
)


"""
Create a package for mathematical operations (add, subtract, multiply, divide)
and use it.
"""
def use_math_package():

    return {
        "add": add(10, 5),
        "subtract": subtract(10, 5),
        "multiply": multiply(10, 5),
        "divide": divide(10, 5)
    }
