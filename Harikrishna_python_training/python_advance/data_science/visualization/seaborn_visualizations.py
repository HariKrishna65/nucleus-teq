import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt


"""
Assignment 6: Seaborn Visualizations
Create employee dataset for advanced charts.
"""
def employee_dataframe() -> pd.DataFrame:
    return pd.DataFrame(
        {
            "Department": ["HR", "IT", "Finance", "IT"],
            "Salary": [30000, 50000, 45000, 60000],
            "Age": [25, 30, 28, 35]
        }
    )


"""
Assignment 6: Seaborn Visualizations
Create Barplot -> Department vs Salary.
"""
def bar_plot() -> None:
    dataframe = employee_dataframe()

    sns.barplot(
        x="Department",
        y="Salary",
        data=dataframe
    )

    plt.show()


"""
Assignment 6: Seaborn Visualizations
Create Boxplot -> Salary distribution.
"""
def box_plot() -> None:
    dataframe = employee_dataframe()

    sns.boxplot(
        y="Salary",
        data=dataframe
    )

    plt.show()


"""
Assignment 6: Seaborn Visualizations
Create Heatmap using correlation between Age and Salary.
"""
def heat_map() -> None:
    dataframe = employee_dataframe()

    sns.heatmap(
        dataframe[["Age", "Salary"]].corr(),
        annot=True
    )

    plt.show()
