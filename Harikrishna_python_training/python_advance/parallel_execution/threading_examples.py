import threading
import time
from concurrent.futures import ThreadPoolExecutor


def print_numbers():

    for number in range(1, 6):
        print(number)


def sum_numbers():

    print(sum(range(1, 101)))


def download_file(name: str):

    time.sleep(2)
    print(f"{name} downloaded")


def square(number: int):

    return number * number