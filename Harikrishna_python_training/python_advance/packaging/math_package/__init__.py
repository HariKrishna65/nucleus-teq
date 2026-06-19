"""
Create a package with two modules and include an __init__.py file.
"""
from python_advance.packaging.math_package.addition import add
from python_advance.packaging.math_package.division import divide
from python_advance.packaging.math_package.multiplication import multiply
from python_advance.packaging.math_package.subtraction import subtract


__all__ = [
    "add",
    "subtract",
    "multiply",
    "divide"
]
