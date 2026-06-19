from python_advance.testing_debugging.prime_checker import is_prime


"""
Write pytest test cases for a function that checks whether a number is prime.
"""
def test_prime():

    assert is_prime(11) is True
    assert is_prime(12) is False
