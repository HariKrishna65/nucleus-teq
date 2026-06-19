import pandas as pd


"""
Assignment 4: Data Analysis
Using employee dataset:
Find average salary by department.
Find max salary by department.
Count employees per department.
"""
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
