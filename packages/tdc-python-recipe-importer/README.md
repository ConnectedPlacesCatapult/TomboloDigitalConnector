# Python Support for Digital Connector

A library to build recipes and importers for Digital Connector using Python.

## Table of Contents:

- [Current Scope](#current-scope)
- [Requirements](#requirements)
- [Installing Virtual Environments](#installing-virtual-environments)
- [Quick Start](#quick-start)
- [Run Recipe](#run-recipe)
- [Implementations](#implementations)
- [How does it work?](#how-does-it-work)
- [Digital Connector](#digital-connector)

## Current Scope
The library currently supports building recipe(Windows, Mac and Linux) and importers(Mac and Linux) through python. Please look through Samples and Importers folder in order to see how it is done.   
We would build more detailed tutorials in the near future as the repository is under development.

*Note: At the moment we don't provide support for contributions and pull requests.*

## Requirements

- Python 3.x (for complete project)
- py4j library (for building importers only)
- [Tombolo Digital Connector](../../README.md)
 - Engine of the Digital Connector is built in Java and is required to run recipe's built in python. To know more 
 about architecture of project, recipe and other components within recipe like Subject, Attribute etc check the 
 [documentation](../../documentation).


## Installing Virtual Environments
- For Windows - [Anaconda](https://conda.io/docs/user-guide/install/index.html#regular-installation)
- For macOS/Ubuntu - [virtualenv](https://virtualenv.pypa.io/en/stable/installation/)/[jupyter](http://jupyter.org/install) 

## Quick Start

This tutorial will guide you to a quick start on **macOS and Ubuntu**.

#### A note about the Terminal

The [Terminal](https://en.wikipedia.org/wiki/Terminal) application can be found in the Applications -> Utilities folder or quickly accessed through Spotlight. It is pre-installed in **macOS** so there is no need to install it.

On **Debian** based systems press ```ctrl+alt+t``` to open Terminal

You will need this application to run some of the commands of this tutorial. When you enter a command and press 
return/enter, the terminal will execute it and complete the task.

**Make sure to press return after typing a command before you enter the next one.**

### Let's start

- Open **Terminal**.
- Follow all the [instructions to install](../../README.md#installation-guides) the Digital Connector based on your 
operating system.

## Run Recipe

- Navigate to the `packages/tdc-python-recipe-importer directory
- To run the sample recipe in the Samples folder, e.g london-no2.py
    - Navigate to Samples folder and open london-no2.py in the editor of your choice. e.g VS Code
    - Change the value of the tombolo_path variable to the path of the installation of Digital Connector
- To execute the recipe, type
```bash
python Samples/london-no2.py
```

**Note: To run the recipe, Digital Connector should be locally installed.**

## How does it work

- Once the recipe is built and ```build_and_run``` is called the python bridge converts the python code into json string. 
- The converted json string is then passed to Digital Connector, as Digital Connector only accepts json file or string as recipe.
- The output of the Digital Connector is then passed to python bridge and displayed in Terminal.

**Note: json string as recipe can only be passed via python script, if it is passed directly the program would produce an error**
