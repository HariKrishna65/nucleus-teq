"""
Create a function with a logical bug and use pdb to identify the issue.
"""
def average_with_bug(numbers: list[int]):

    import pdb

    pdb.set_trace()
    total = sum(numbers)

    return total / len(numbers) + 1


"""
Use pdb breakpoints inside a loop and inspect variable values.
"""
def inspect_loop_values(numbers: list[int]):

    import pdb

    total = 0

    for number in numbers:
        pdb.set_trace()
        total += number

    return total


"""
Explain the advantages of using an IDE debugger over print statements.
"""
def ide_debugger_advantages():

    return (
        "An IDE debugger lets you pause execution, inspect variables, step "
        "through code line by line, watch expressions, and understand program "
        "flow without adding and removing many print statements."
    )
