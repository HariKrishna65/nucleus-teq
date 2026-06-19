import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from python_advance.data_science.constants import PASS_MARKS


"""
Assignment 7: Mini Project
Create a dataset of Students with Name, Marks, and Hours Studied.
Load into Pandas.
Add Performance = Pass if Marks > 65 else Fail.
"""
def create_student_dataframe() -> pd.DataFrame:
    dataframe = pd.DataFrame(
        {
            "Name": ["Rahul", "Priya", "Siri", "Anuj"],
            "Marks": [70, 80, 90, 60],
            "Hours Studied": [2, 3, 5, 1]
        }
    )

    dataframe["Performance"] = dataframe["Marks"].apply(
        lambda marks: (
            "Pass"
            if marks > PASS_MARKS
            else "Fail"
        )
    )

    return dataframe


"""
Assignment 7: Mini Project
Visualize Line Chart -> Hours vs Marks.
"""
def line_chart() -> None:
    dataframe = create_student_dataframe()

    plt.plot(
        dataframe["Hours Studied"],
        dataframe["Marks"]
    )

    plt.show()


"""
Assignment 7: Mini Project
Visualize Scatter Plot -> Study vs Marks.
"""
def scatter_plot() -> None:
    dataframe = create_student_dataframe()

    plt.scatter(
        dataframe["Hours Studied"],
        dataframe["Marks"]
    )

    plt.show()


"""
Assignment 7: Mini Project
Use Seaborn Barplot -> Performance vs Marks.
"""
def performance_barplot() -> None:
    dataframe = create_student_dataframe()

    sns.barplot(
        x="Performance",
        y="Marks",
        data=dataframe
    )

    plt.show()
