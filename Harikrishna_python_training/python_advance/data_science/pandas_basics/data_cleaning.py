import pandas as pd


"""
Assignment 3: Data Cleaning
Create dataset with Name, Age, and Salary.
Detect missing values.
Replace missing Age with mean.
Replace missing Salary with 0.
"""
def clean_data() -> pd.DataFrame:
    dataframe = pd.DataFrame(
        {
            "Name": ["Rahul", "Priya", "Anuj"],
            "Age": [25, None, 29],
            "Salary": [30000, 40000, None]
        }
    )

    print(dataframe.isnull())

    dataframe["Age"] = dataframe["Age"].fillna(
        dataframe["Age"].mean()
    )

    dataframe["Salary"] = dataframe["Salary"].fillna(0)

    return dataframe
