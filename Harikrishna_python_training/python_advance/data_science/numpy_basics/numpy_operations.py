import numpy as np


"""
Assignment 1: NumPy Basics
Create a NumPy array: [10, 20, 30, 40, 50]
Perform Mean, Max, Min, and Sum.
"""
def calculate_statistics() -> dict:
    numbers = np.array([10, 20, 30, 40, 50])

    return {
        "mean": np.mean(numbers),
        "max": np.max(numbers),
        "min": np.min(numbers),
        "sum": np.sum(numbers)
    }


"""
Assignment 1: NumPy Basics
Create two arrays:
arr_1 = [1, 2, 3]
arr_2 = [4, 5, 6]
Perform Addition and Multiplication.
"""
def perform_array_operations() -> dict:
    arr_1 = np.array([1, 2, 3])
    arr_2 = np.array([4, 5, 6])

    return {
        "addition": arr_1 + arr_2,
        "multiplication": arr_1 * arr_2
    }


"""
Assignment 1: NumPy Basics
Create a 3x3 matrix using NumPy.
"""
def create_matrix() -> np.ndarray:
    return np.array(
        [
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]
        ]
    )
