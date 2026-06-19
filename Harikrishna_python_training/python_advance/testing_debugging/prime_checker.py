def is_prime(number: int):

    if number <= 1:
        return False

    for value in range(
        2,
        int(number ** 0.5) + 1
    ):
        if number % value == 0:
            return False

    return True