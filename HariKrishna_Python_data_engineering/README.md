# Member Data Management System (Windows Edition)

A modular, production-ready Python utility built to clean, sanitize, validate, and store member profiles from unstructured or raw dictionary datasets on Windows systems. This project satisfies all integrated core concepts required by the Python Advanced Training Module.

---

## 🚀 Key Features

- **Robust Validation Engine:** Uses Regular Expressions (regex) to safely catch and flag malformed emails and phone digits.
- **Custom Error Profiles:** Uses custom exception inheritance structures to avoid silent script failures.
- **Functional Paradigm Slicing:** Embeds clean data filtering routines using Lambda transformations over active collections.
- **Modern Packaging Architecture:** Built with native `pyproject.toml` specs and bundled cleanly into a portable `.whl` (Wheel) binary package.

---

## 📁 Project Directory Blueprint

```text
member_processor_project/
├── my_processor/                # Package Source Directory
│   ├── __init__.py              # Package entry point & root API mapping
│   ├── core.py                  # OOP Data Models & Pipeline Processing Logic
│   └── utils.py                 # Core Regular Expressions & Exception Handlers
├── pyproject.toml               # Modern Build System Metadata configuration
├── setup.py                     # Retrocompatible Setuptools distribution script
└── README.md                    # Technical project documentation
```

---

## 🛠️ Windows Step-by-Step Deployment Guide

### 1. Isolated Sandbox Initialization
Open your Windows Terminal (PowerShell or Command Prompt) inside your project root directory and initialize your virtual environment:

```powershell
# 1. Create the environment
python -m venv venv

# 2. Activate the environment
# For PowerShell:
.\venv\Scripts\Activate.ps1
# For Command Prompt (cmd):
.\venv\Scripts\activate.bat

# 3. Keep underlying builder modules current
python -m pip install --upgrade pip setuptools wheel
```

⚠️ **Windows Note:** If you get a script execution error in PowerShell, run this command first to allow local script execution: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope Process`

### 2. Compile the Wheel Asset
Execute the local compilation workflow directly from the project root directory path:

```powershell
python setup.py sdist bdist_wheel
```
*Executing this action will output your final binary distribution archive into a newly generated `dist\` directory.*

### 3. Deploy the Local Wheel File
Install the newly created Wheel file directly onto your environment path via pip:

```powershell
pip install dist\data_processor_task-1.0.0-py3-none-any.whl
```

---

## 💻 Sample Program Usage

Once installed, your package interface can be accessed across any system file pathway. Below is an integration blueprint demonstrating its parsing capabilities:

```python
from my_processor import Member, process_raw_data, filter_members_by_domain

# Define the assignment test baseline inputs
raw_members = [
    {"name": "John Doe", "email": "john.doe@example.com", "phone": "555-0101"},
    {"name": "Jane Smith", "email": "jane.smith@...com", "phone": "555-0102"},
    {"name": "Malicious Record", "email": "hacker@badurl.", "phone": "9-1-1"}
]

print("=== Starting Data Verification Sequence ===")
# Parse arrays using the Core Processor logic
cleaned_profiles = process_raw_data(raw_members)

print("\n=== Executing Functional Lambda Domain Sort ===")
filtered_list = filter_members_by_domain(cleaned_profiles, "example.com")
for profile in filtered_list:
    print(f"Match Discovered: {profile}")
```

---

## 🧪 Expected Console Outputs

```text
=== Starting Data Verification Sequence ===
Processing member: John Doe...
Validation Successful.
Processing member: Jane Smith...
Shell Python Error: Invalid email for member 'Jane Smith'. Skipping.
Processing member: Malicious Record...
Shell Python Error: Invalid email for member 'Malicious Record'. Skipping.

Summary: 1 members processed successfully.

=== Executing Functional Lambda Domain Sort ===
Match Discovered: Member(Name: John Doe, Email: john.doe@example.com, Phone: 555-0101)
```
