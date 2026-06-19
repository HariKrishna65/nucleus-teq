"""Data cleaning examples."""

import pandas as pd


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