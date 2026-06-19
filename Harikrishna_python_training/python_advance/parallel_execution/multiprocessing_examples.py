import os
from multiprocessing import Process
from concurrent.futures import ProcessPoolExecutor


def process_info():

    print(
        "PID:",
        os.getpid()
    )


def square(number: int):

    print(
        number,
        number * number
    )