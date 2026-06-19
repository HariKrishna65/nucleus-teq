import pandas as pd

from python_advance.data_science.constants import BONUS_PERCENTAGE


"""
Assignment 2: Pandas DataFrame Creation
Create a DataFrame for employees with Name, Age, Department, and Salary.
Add new column: Bonus = Salary * 0.10.
"""
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


"""
Assignment 2: Pandas DataFrame Creation
Show first 2 rows.
"""
def first_two_rows(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe.head(2)


"""
Assignment 2: Pandas DataFrame Creation
Show summary statistics.
"""
def summary_statistics(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe.describe()


"""
Assignment 2: Pandas DataFrame Creation
Display only IT employees.
"""
def get_it_employees(dataframe: pd.DataFrame) -> pd.DataFrame:
    return dataframe[dataframe["Department"] == "IT"]
