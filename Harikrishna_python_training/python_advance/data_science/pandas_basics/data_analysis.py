"""GroupBy analysis."""

import pandas as pd


def department_analysis() -> dict:
    dataframe = pd.DataFrame(
        {
            "Department": ["HR", "IT", "Finance", "IT"],
            "Salary": [30000, 50000, 45000, 60000]
        }
    )

    return {
        "average_salary":
            dataframe.groupby("Department")["Salary"].mean(),

        "max_salary":
            dataframe.groupby("Department")["Salary"].max(),

        "employee_count":
            dataframe.groupby("Department").size()
    }