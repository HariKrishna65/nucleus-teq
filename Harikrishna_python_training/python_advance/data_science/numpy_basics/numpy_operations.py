"""NumPy operations."""

import numpy as np


def calculate_statistics() -> dict:
    numbers = np.array([10, 20, 30, 40, 50])

    return {
        "mean": np.mean(numbers),
        "max": np.max(numbers),
        "min": np.min(numbers),
        "sum": np.sum(numbers)
    }


def perform_array_operations() -> dict:
    arr_1 = np.array([1, 2, 3])
    arr_2 = np.array([4, 5, 6])

    return {
        "addition": arr_1 + arr_2,
        "multiplication": arr_1 * arr_2
    }


def create_matrix() -> np.ndarray:
    return np.array(
        [
            [1, 2, 3],
            [4, 5, 6],
            [7, 8, 9]
        ]
    )