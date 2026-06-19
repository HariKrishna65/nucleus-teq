import threading
import time
from concurrent.futures import ThreadPoolExecutor


"""
Write a program to create two threads that print numbers from 1 to 5
simultaneously.
"""
def print_numbers():

    for number in range(1, 6):
        print(number)


def run_two_threads():

    first_thread = threading.Thread(target=print_numbers)
    second_thread = threading.Thread(target=print_numbers)

    first_thread.start()
    second_thread.start()

    first_thread.join()
    second_thread.join()


"""
Create a thread that calculates the sum of numbers from 1 to 100.
"""
def sum_numbers():

    print(sum(range(1, 101)))


def run_sum_thread():

    thread = threading.Thread(target=sum_numbers)
    thread.start()
    thread.join()


"""
Demonstrate the use of join() method in threading.
"""
def demonstrate_join():

    thread = threading.Thread(target=print_numbers)
    thread.start()
    thread.join()

    print("Thread completed")


"""
Create multiple threads to simulate file downloading using time.sleep().
"""
def download_file(name: str):

    time.sleep(2)
    print(f"{name} downloaded")


def simulate_downloads():

    threads = [
        threading.Thread(
            target=download_file,
            args=(f"file_{number}",)
        )
        for number in range(1, 4)
    ]

    for thread in threads:
        thread.start()

    for thread in threads:
        thread.join()


"""
Convert a normal function into parallel execution using ThreadPoolExecutor.
"""
def square(number: int):

    return number * number


def square_with_thread_pool(numbers: list[int]):

    with ThreadPoolExecutor() as executor:
        return list(
            executor.map(
                square,
                numbers
            )
        )
