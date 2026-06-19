"""Pandas DataFrame creation."""

import pandas as pd

from python_advance.data_science.constants import BONUS_PERCENTAGE


def create_employee_dataframe() -> pd.DataFrame:
    employee_dataframe = pd.DataFrame(
        {
            "Name": ["Rahul", "Priya", "Amit", "Anuj"],
            "Age": [25, 30, 28, 35],
            "Department": ["HR", "IT", "Finance", "IT"],
            "Salary": [30000, 50000, 45000, 60000]
        }
    )

    employee_dataframe["Bonus"] = (
        employee_dataframe["Salary"] * BONUS_PERCENTAGE
    )

    return employee_dataframe


def first_two_rows(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe.head(2)


def summary_statistics(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe.describe()


def get_it_employees(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe[dataframe["Department"] == "IT"]