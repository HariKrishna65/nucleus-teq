import re


def extract_numbers(text: str):

    return re.findall(r"\d+", text)


def validate_email(email: str):

    pattern = (
        r"^[A-Za-z0-9._%+-]+"
        r"@[A-Za-z0-9.-]+"
        r"\.[A-Za-z]{2,}$"
    )

    return bool(
        re.match(pattern, email)
    )


def validate_mobile(number: str):

    return bool(
        re.match(
            r"^[0-9]{10}$",
            number
        )
    )


def search_word(
    sentence: str,
    word: str
):

    return bool(
        re.search(word, sentence)
    )


def capital_words(text: str):

    return re.findall(
        r"\b[A-Z][a-zA-Z]*\b",
        text
    )


def remove_extra_spaces(text: str):

    return re.sub(
        r"\s+",
        " ",
        text
    )


def alphabet_only(text: str):

    return bool(
        re.match(
            r"^[A-Za-z]+$",
            text
        )
    )