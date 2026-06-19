"""Student performance project."""

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

from python_advance.data_science.constants import PASS_MARKS


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


def line_chart() -> None:
    dataframe = create_student_dataframe()

    plt.plot(
        dataframe["Hours Studied"],
        dataframe["Marks"]
    )

    plt.show()


def scatter_plot() -> None:
    dataframe = create_student_dataframe()

    plt.scatter(
        dataframe["Hours Studied"],
        dataframe["Marks"]
    )

    plt.show()


def performance_barplot() -> None:
    dataframe = create_student_dataframe()

    sns.barplot(
        x="Performance",
        y="Marks",
        data=dataframe
    )

    plt.show()