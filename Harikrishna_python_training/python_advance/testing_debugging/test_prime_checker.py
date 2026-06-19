from python_advance.testing_debugging.prime_checker import is_prime


def test_prime():

    assert is_prime(11) is True
    assert is_prime(12) is False