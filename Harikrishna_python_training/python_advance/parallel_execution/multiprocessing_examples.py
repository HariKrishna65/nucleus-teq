import os
from multiprocessing import Process
from concurrent.futures import ProcessPoolExecutor


"""
Write a program to create two processes that print their Process IDs.
"""
def process_info():

    print(
        "PID:",
        os.getpid()
    )


def run_two_processes():

    first_process = Process(target=process_info)
    second_process = Process(target=process_info)

    first_process.start()
    second_process.start()

    first_process.join()
    second_process.join()


"""
Write a multiprocessing program to calculate the square of numbers using
Process class.
"""
def square(number: int):

    print(
        number,
        number * number
    )


def square_with_processes(numbers: list[int]):

    processes = [
        Process(
            target=square,
            args=(number,)
        )
        for number in numbers
    ]

    for process in processes:
        process.start()

    for process in processes:
        process.join()


"""
Convert a normal function into parallel execution using ProcessPoolExecutor.
"""
def calculate_square(number: int):

    return number * number


def square_with_process_pool(numbers: list[int]):

    with ProcessPoolExecutor() as executor:
        return list(
            executor.map(
                calculate_square,
                numbers
            )
        )
