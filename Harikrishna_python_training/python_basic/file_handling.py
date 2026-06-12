"""File Handling"""

from python_basic.constants import FILE_NAME


def write_name(
    name: str
) -> None:

    with open(
        FILE_NAME,
        "w",
        encoding="utf-8"
    ) as file:
        file.write(name)


def file_statistics() -> dict:

    with open(
        FILE_NAME,
        "r",
        encoding="utf-8"
    ) as file:
        content = file.read()

    return {
        "words": len(content.split()),
        "characters": len(content),
        "lines": len(content.splitlines())
    }


def append_text(
    text: str
) -> None:

    with open(
        FILE_NAME,
        "a",
        encoding="utf-8"
    ) as file:
        file.write(
            "\n" + text
        )


def copy_file(
    source_file: str,
    destination_file: str
) -> None:

    with open(
        source_file,
        "r",
        encoding="utf-8"
    ) as source:

        content = source.read()

    with open(
        destination_file,
        "w",
        encoding="utf-8"
    ) as destination:

        destination.write(content)


def search_word(
    word: str
) -> bool:

    with open(
        FILE_NAME,
        "r",
        encoding="utf-8"
    ) as file:

        return word in file.read()