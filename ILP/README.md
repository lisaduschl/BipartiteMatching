# Installation

1) Get python 3.7 and python3.7-venv (not compatible with earlier versions)

2) Create a virtual environment:

$ python3.7 -m venv .venv
$ source .venv/bin/activate


3) Install dependencies

$ pip install -r requirements.txt


4) Use
```
$ python main.py --help
Usage: main.py [OPTIONS]

Options:
  --complete             Use a complete graph
  --solver [cbc|gurobi]  Choose the ILP solver
  --output                Show solver output
  --timeout INTEGER      Timeout for the ILP solver
  --help                 Show this message and exit.
```
