"""OOP Examples"""


class Student:

    def __init__(
        self,
        name: str,
        age: int
    ):

        self.name = name
        self.age = age

    def display_details(
        self
    ) -> str:

        return (
            f"Name: {self.name}, "
            f"Age: {self.age}"
        )


class Car:

    def __init__(
        self,
        brand: str
    ):

        self.brand = brand


class Person:

    def __init__(
        self,
        name: str
    ):

        self.name = name


class Employee(Person):

    def __init__(
        self,
        name: str,
        salary: float
    ):

        super().__init__(name)
        self.salary = salary


class Bank:

    def __init__(self):

        self.__balance = 0

    def deposit(
        self,
        amount: float
    ) -> None:

        self.__balance += amount

    def get_balance(
        self
    ) -> float:

        return self.__balance


class Dog:

    def speak(self) -> str:
        return "Bark"


class Cat:

    def speak(self) -> str:
        return "Meow"