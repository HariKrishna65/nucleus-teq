"""Data Structures"""


def list_operations(
    numbers: list[int]
) -> dict:

    return {
        "sum": sum(numbers),
        "maximum": max(numbers),
        "sorted": sorted(numbers),
        "unique": list(set(numbers))
    }


def count_even_odd(
    numbers: list[int]
) -> dict:

    even_count = sum(
        1
        for number in numbers
        if number % 2 == 0
    )

    return {
        "even": even_count,
        "odd": len(numbers) - even_count
    }


def reverse_list(
    numbers: list[int]
) -> list[int]:

    return numbers[::-1]


def tuple_access() -> tuple:

    return (
        "Hari",
        21,
        "CSE"
    )


def tuple_to_list() -> list:

    values = (
        1,
        2,
        3
    )

    converted_list = list(values)
    converted_list.append(4)

    return converted_list


def set_operations(
    first_set: set,
    second_set: set
) -> dict:

    return {
        "union": first_set | second_set,
        "intersection": first_set & second_set,
        "difference": first_set - second_set
    }


def remove_duplicates(
    values: list
) -> list:

    return list(set(values))


def student_dictionary() -> dict:

    return {
        "name": "Hari",
        "age": 21,
        "course": "CSE"
    }


def character_frequency(
    text: str
) -> dict:

    frequency = {}

    for character in text:
        frequency[character] = (
            frequency.get(character, 0) + 1
        )

    return frequency


def merge_dictionaries(
    first_dictionary: dict,
    second_dictionary: dict
) -> dict:

    return (
        first_dictionary |
        second_dictionary
    )