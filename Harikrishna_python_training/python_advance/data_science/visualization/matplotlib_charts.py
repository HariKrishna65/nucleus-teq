"""Matplotlib charts."""

import matplotlib.pyplot as plt


def bar_chart() -> None:
    departments = ["HR", "IT", "Finance"]
    employees = [5, 12, 7]

    plt.bar(departments, employees)
    plt.title("Employees by Department")
    plt.show()


def line_chart() -> None:
    departments = ["HR", "IT", "Finance"]
    employees = [5, 12, 7]

    plt.plot(departments, employees)
    plt.show()


def histogram() -> None:
    salaries = [30000, 40000, 50000, 60000, 45000]

    plt.hist(salaries)
    plt.show()


def scatter_plot() -> None:
    ages = [25, 30, 28, 35]
    salaries = [30000, 50000, 45000, 60000]

    plt.scatter(ages, salaries)
    plt.xlabel("Age")
    plt.ylabel("Salary")
    plt.show()