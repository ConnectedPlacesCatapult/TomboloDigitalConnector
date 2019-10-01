# Tombolo Digital Connector QGIS plugin
A plugin for viewing, modifying and running the Tombolo Digital Connector through QGIS. The Tombolo Digital Connector is an open source software that allows automatic fetching, cleaning and combining of spatial data from different sources and different specifications. For more information please visit [Digital Connector](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector). 

**Please note** This plugin requires a complete installation of the Digital Connector. 

Currently, the Digital Connector QGIS plugin has the following functionalities:
* It allows the user to run pre-build recipes without interacting with the terminal window and loads back the resulting geojson
* It allows altering the existing recipes in a more intuitive way
* It allows saving the modified recipes in a seperate file
* It allows visualising the existing/modified recipes in a UML-based format

## Table of Contents:

- [Current Scope](#current-scope)
- [Requirements](#requirements)
- [Installation](#Installation)
- [Quick Start](#quick-start)
- [Digital Connector](#digital-connector)
- [License](#license) 

## Current Scope
The plugin have been tested for both Windows and Mac. Although it should be fully functional on Linux, it has not been tested for Debian based systems.

## Requirements
* Installation of the Digital Connector. This includes Java, gradle and git. In the case these are not installed in the default installation folders (eg. Program Files for Windows) the plugin will prompt the user to select the folder manually. In this case, navigate to the **\bin** folder of the software (eg. for gradle you should specify \path_to_gradle\gradle\bin) 
* Installation of QGIS 2.x. The plugin does not support QGIS 3. You can download QGIS 2.x from [here](https://qgis.org/en/site/forusers/download.html). **Please make sure you choose version 2**
* Installation of Graphviz for recipe visualisation. Detailed instruction on how to install Graphviz are included in the Installation section.

## Installation

### QGIS plugin
Currently the plugin is not on QGIS Python Plugins Repository. To install it:

* Clone this repo in /your_path_to_qgis/.qgis2/python/plugins by typing ``git clone https://github.com/FutureCitiesCatapult/DigitalConnectorPlugin.git`` either in the terminal window (Mac OSX) or the command line (Windows).
* If the plugins folder is missing, then you have to manually create by right clicking in the /your_path_to_qgis/.qgis2/python/ directory and clicking New Folder
* If you are on *Windows* and assuming you have a working installation of QGIS 2.x on your computer:
* Launch OSGeo4W Shell by typing ``osgeo4w`` in Windows finder
![Alt text](/img/osgeo4.png)
* Navigate to the QGIS plugin folder by typing 
``cd Users\your_name\.qgis2\python\plugins\DigitalConnectorPlugin``
![Alt text](/img/cd.png)
* Compile the plugin by typing 
``pyrcc4 -o resources.py resources.qrc``
![Alt text](/img/compile.png)

If you are on *Mac*:
* Install Qt4 by typing in the terminal window ```brew install cartr/qt4/pyqt```
* Navigate to qgis plugin folder. Usually this is ```cd ~/.qgis2/python/plugins/DigitalConnectorPlugin```
* Compile the resources file using pyrcc4 by typing in the terminal window ```pyrcc4 -o resources.py resources.qrc ```
* Run the tests (``make test``)
* Test the plugin by enabling it in the QGIS plugin manager

### Graphviz
If you are on *Windows*:
* Download and install the stable version of Graphviz from [here](https://graphviz.gitlab.io/_pages/Download/Download_windows.html). It is highly recommended that you use the default directories for the installation.
* Download pip from [here](https://bootstrap.pypa.io/get-pip.py)
* Launch OSGeo4W Shell as administrator by typing ``osgeo4w`` in Windows finder and then right click on ``Run as administrator``
![Alt text](/img/run_admin.png)
* Navigate to *get-pip.py* file by typing ``cd path_to\get-pip.py`` in the OSGeo4W Shell.
* Install pip by typing ``python get-pip.py`` in the OSGeo4W Shell.
* Install graphviz by typing ``pip install graphviz`` in the OSGeo4W Shell

If you are on *Mac*:
* Install graphviz by typing ``brew install graphviz`` in the terminal window
* If you have multiple distributions of python for your computer, find out which one corresponds to QGIS by:
  * Launch QGIS
  * From within QGIS locate the Python concole icon ![Alt text](/img/python_console.png). Clicking it will open Python console
  * In the Python console type ``import sys`` following by ``print sys.executable``. The directory that comes up is the directory of QGIS Python distribution. Make a note of this path (eg. /usr/bin/python)
* Download pip from [here](https://bootstrap.pypa.io/get-pip.py)
* Navigate to *get-pip.py* file by typing ``cd path_to\get-pip.py`` in the terminal.
* Install pip by typing ``<replace_it_with_python_path_obtained_above> get-pip.py`` in the terminal.
* Install graphviz by typing ``<replace_it_with_python_path_obtained_above> -m pip install graphviz`` in the terminal

## Quick Start
Open QGIS and click on the plugin manager located on the toolbar

![Alt text](/img/1.png)

In the popup window, type **Digital Connector Plugin** and enable the plugin by clicking on the checkbox next to it

![Alt text](/img/2.png)
In the main plugin window, browse to the local copy of Tombolo Digital Connector by clicking ``...``

![Alt text](/img/3.png)

All existing recipes in the Digital Connector's example folder will be loaded in a dropdown box

![Alt text](/img/5.png)

Clicking the **View recipe** button will render the UML version of the recipe. This allows the user to explore how the different subjects/datasources/fields are linked together

![Alt text](/img/7.png)

By clicking the **Edit recipe** button a popup window will appear the wraps the recipe's subjects/datasources/fields in dropdown boxes 

![Alt text](/img/8.png)

Expanding this allows the user to directly edit the contents of the recipe. By clicking the **Save** button the recipe can be saved locally

![Alt text](/img/9.png)

By clicking the **Run** button the Digital Connector's command ``gradle -runExport`` is invoked.  

![Alt text](/img/5.png)

Once the process is complete, the output file will be loaded automatically in QGIS's Layer Panel  

![Alt text](/img/11.png)
