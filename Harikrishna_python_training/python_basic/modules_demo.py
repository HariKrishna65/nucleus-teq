"""Modules Example"""

import math
import random

from python_basic.constants import (
    RANDOM_START,
    RANDOM_END
)


def math_operations(number: int) -> dict:

    return {
        "square_root": math.sqrt(number),
        "power": math.pow(number, 2),
        "factorial": math.factorial(number)
    }


def generate_random_number() -> int:

    return random.randint(
        RANDOM_START,
        RANDOM_END
    )