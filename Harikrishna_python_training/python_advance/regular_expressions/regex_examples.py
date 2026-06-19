import re


"""
Write a program to extract all numbers from a given string using regular
expressions.
"""
def extract_numbers(text: str):

    return re.findall(r"\d+", text)


"""
Write a regular expression to validate an email address.
"""
def validate_email(email: str):

    pattern = (
        r"^[A-Za-z0-9._%+-]+"
        r"@[A-Za-z0-9.-]+"
        r"\.[A-Za-z]{2,}$"
    )

    return bool(
        re.match(pattern, email)
    )


"""
Write a regular expression to validate a 10-digit mobile number.
"""
def validate_mobile(number: str):

    return bool(
        re.match(
            r"^[0-9]{10}$",
            number
        )
    )


"""
Use re.search() to check whether a word exists in a sentence.
"""
def search_word(
    sentence: str,
    word: str
):

    return bool(
        re.search(word, sentence)
    )


"""
Use re.findall() to extract all words starting with a capital letter.
"""
def capital_words(text: str):

    return re.findall(
        r"\b[A-Z][a-zA-Z]*\b",
        text
    )


"""
Replace multiple spaces in a string with a single space using re.sub().
"""
def remove_extra_spaces(text: str):

    return re.sub(
        r"\s+",
        " ",
        text
    )


"""
Write a pattern to check if a string contains only alphabets.
"""
def alphabet_only(text: str):

    return bool(
        re.match(
            r"^[A-Za-z]+$",
            text
        )
    )


"""
Create a password validation program using regex (minimum length, one digit,
one special character).
"""
def validate_password(password: str):

    pattern = (
        r"^(?=.*\d)"
        r"(?=.*[^A-Za-z0-9])"
        r".{8,}$"
    )

    return bool(
        re.match(
            pattern,
            password
        )
    )
