# -*- coding: utf-8 -*-
"""
/***************************************************************************
 DigitalConnectorPlugin
                                 A QGIS plugin
 Digital Connector Plugin
                              -------------------
        begin                : 2018-03-22
        git sha              : $Format:%H$
        copyright            : (C) 2018 by Future Cities Catapult
        email                : tbantis@futurecities.catapult.org.uk
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
"""
from PyQt4.QtCore import QSettings, QTranslator, qVersion, QCoreApplication, QProcess
from PyQt4.QtGui import QAction, QIcon, QFileDialog, QLabel, QPixmap, QLineEdit, QProgressBar, QPushButton, QGridLayout, QWidget,QMainWindow
from qgis.core import QgsMessageLog, QgsVectorLayer, QgsMapLayerRegistry
from qgis.gui import QgsMessageBar
from processing.gui.MessageDialog import MessageDialog
# Initialize Qt resources from file resources.py
import resources
# Import the code for the dialog
from digital_connector_plugin_dialog import DigitalConnectorPluginDialog, EditRecipe

import os.path
from os.path import expanduser
import subprocess as sp
import json
import matplotlib.pyplot as plt
import matplotlib.image as mpimg
import os
import sys
import time
import json
import fileinput
import subprocess as sp
import uuid
import platform

class DigitalConnectorPlugin:
    """QGIS Plugin Implementation."""

    def __init__(self, iface):
        """Constructor.

        :param iface: An interface instance that will be passed to this class
            which provides the hook by which you can manipulate the QGIS
            application at run time.
        :type iface: QgsInterface
        """
        # Save reference to the QGIS interface
        self.iface = iface
        # initialize plugin directory
        self.plugin_dir = os.path.dirname(__file__)
        # initialize locale
        locale = QSettings().value('locale/userLocale')[0:2]
        locale_path = os.path.join(
            self.plugin_dir,
            'i18n',
            'DigitalConnectorPlugin_{}.qm'.format(locale))

        if os.path.exists(locale_path):
            self.translator = QTranslator()
            self.translator.load(locale_path)

            if qVersion() > '4.3.3':
                QCoreApplication.installTranslator(self.translator)

        ##### Windows configuration
        # check for JAVA in Program Files
        if platform.system() == 'Windows':
            java_path = None
            # Look in both Program Files and Program Files x86
            for i in os.listdir('C:\\Program Files'):
                if 'Java' in i:
                    java_path_temp = 'C:\\Program Files\\' + i 
                    for k in os.listdir(java_path_temp):
                        if 'jdk' in k:
                            java_path_temp = java_path_temp + '\\'  + k + '\\bin'
                    java_path = java_path_temp
                else:
                    pass

            if java_path != None:
                pass
            else:
                for j in  os.listdir('C:\\Program Files (x86)'):
                    if 'Java' in j:
                        java_path_temp = 'C:\\Program Files (x86)\\' + j 
                        for k in os.listdir(java_path_temp):
                            if 'jdk' in k:
                                java_path_temp = java_path_temp + '\\'  + k + '\\bin'
                        java_path = java_path_temp
                    else:
                        pass
            # ERROR cannot find Java installation
            if java_path == None:
                self.iface.messageBar().pushMessage("Error", "No Java installation found in Program Files. Please install Java", level=QgsMessageBar.CRITICAL)

            # Add missing PATHs for windows
            current_execs = os.environ['PATH']
            if not 'Java' in current_execs:
                os.environ['PATH'] += ';' + java_path

        #####




        # Declare instance attributes
        self.actions = []
        self.menu = self.tr(u'&Digital Connector Plugin')
        # TODO: We are going to let the user set this up in a future iteration
        self.toolbar = self.iface.addToolBar(u'DigitalConnectorPlugin')
        self.toolbar.setObjectName(u'DigitalConnectorPlugin')

        # Edit recipe dialog class
        self.dialog_instance = EditRecipe()

        # Create the dialog (after translation) and keep reference
        self.dlg = DigitalConnectorPluginDialog()

        # Add button functionalities
        self.dlg.lineEdit.clear()
        self.dlg.pushButton.clicked.connect(self.select_dc_directory)
        self.dlg.pushButton_2.clicked.connect(self.visualise_recipe)
        self.dlg.pushButton_3.clicked.connect(self.edit_recipe)

        # Add DC icon
        img_path = self.resolve('dc_logo.png')
        print img_path
        self.dlg.label_3.setPixmap(QPixmap(img_path))


    # noinspection PyMethodMayBeStatic
    def tr(self, message):
        """Get the translation for a string using Qt translation API.

        We implement this ourselves since we do not inherit QObject.

        :param message: String for translation.
        :type message: str, QString

        :returns: Translated version of message.
        :rtype: QString
        """
        # noinspection PyTypeChecker,PyArgumentList,PyCallByClass
        return QCoreApplication.translate('DigitalConnectorPlugin', message)


    def add_action(
        self,
        icon_path,
        text,
        callback,
        enabled_flag=True,
        add_to_menu=True,
        add_to_toolbar=True,
        status_tip=None,
        whats_this=None,
        parent=None):
        """Add a toolbar icon to the toolbar.

        :param icon_path: Path to the icon for this action. Can be a resource
            path (e.g. ':/plugins/foo/bar.png') or a normal file system path.
        :type icon_path: str

        :param text: Text that should be shown in menu items for this action.
        :type text: str

        :param callback: Function to be called when the action is triggered.
        :type callback: function

        :param enabled_flag: A flag indicating if the action should be enabled
            by default. Defaults to True.
        :type enabled_flag: bool

        :param add_to_menu: Flag indicating whether the action should also
            be added to the menu. Defaults to True.
        :type add_to_menu: bool

        :param add_to_toolbar: Flag indicating whether the action should also
            be added to the toolbar. Defaults to True.
        :type add_to_toolbar: bool

        :param status_tip: Optional text to show in a popup when mouse pointer
            hovers over the action.
        :type status_tip: str

        :param parent: Parent widget for the new action. Defaults None.
        :type parent: QWidget

        :param whats_this: Optional text to show in the status bar when the
            mouse pointer hovers over the action.

        :returns: The action that was created. Note that the action is also
            added to self.actions list.
        :rtype: QAction
        """

        icon = QIcon(icon_path)
        action = QAction(icon, text, parent)
        action.triggered.connect(callback)
        action.setEnabled(enabled_flag)

        if status_tip is not None:
            action.setStatusTip(status_tip)

        if whats_this is not None:
            action.setWhatsThis(whats_this)

        if add_to_toolbar:
            self.toolbar.addAction(action)

        if add_to_menu:
            self.iface.addPluginToMenu(
                self.menu,
                action)

        self.actions.append(action)

        return action

    def initGui(self):
        """Create the menu entries and toolbar icons inside the QGIS GUI."""

        icon_path = ':/plugins/DigitalConnectorPlugin/icon.png'
        self.add_action(
            icon_path,
            text=self.tr(u'Digital Connector Plugin'),
            callback=self.run,
            parent=self.iface.mainWindow())


    def unload(self):
        """Removes the plugin menu item and icon from QGIS GUI."""
        for action in self.actions:
            self.iface.removePluginMenu(
                self.tr(u'&Digital Connector Plugin'),
                action)
            self.iface.removeToolBarIcon(action)
        # remove the toolbar
        del self.toolbar

    def select_dc_directory(self):
        """Selects the DC directory"""

        filename = QFileDialog.getExistingDirectory(
                    self.dlg,
                    "Open a folder",
                    expanduser("~"),
                    QFileDialog.ShowDirsOnly
                )
        # check wheather cancel button was pushed
        if filename:
            self.dlg.lineEdit.setText(filename)
            recipes = filename + "/src/main/resources/executions/examples"
            recipes_list = []
            for file in os.listdir(recipes):
                if file.endswith(".json"):
                    recipes_list.append(file)

            # Attach signal to the combobox
            self.dlg.comboBox.currentIndexChanged.connect(self.on_combobox_changed)    

            self.dlg.comboBox.clear()
            self.dlg.comboBox.addItems(recipes_list)

    # Get the path to gradle per os
    def get_gradle_dir(self):
        """Searches for gradle. If not found it asks for user input"""

        gradle_path = None
        # Windows
        if platform.system() == 'Windows':
            # Look in both Program Files and Program Files x86
            for i in os.listdir('C:\\Program Files'):
                if 'gradle' in i:
                    gradle_path = 'C:\\Program Files\\' + i
                    # Look for one more nesting
                    if 'bin' in os.listdir(gradle_path):
                        gradle_path = 'C:\\Program Files\\' + i + '\\bin'
                    else:
                        for k in os.listdir(gradle_path):
                            if 'gradle' in k:
                                gradle_path = 'C:\\Program Files\\' + i + '\\' + k + '\\bin'
                    return gradle_path
                else:
                    gradle_path == None

            for j in  os.listdir('C:\\Program Files (x86)'):
                if 'gradle' in j:
                    gradle_path = 'C:\\Program Files (x86)\\' + j + '\\bin'
                    return gradle_path
                else:
                    gradle_path == None
                                  
            if gradle_path == None:
                gradle_path = QFileDialog.getExistingDirectory(
                        self.dlg,
                        "Select gradle path",
                        expanduser("~"),
                        QFileDialog.ShowDirsOnly
                    )
                return  gradle_path
        # MacOSX 
        elif platform.system() == 'Darwin':
            for i in os.listdir('/usr/local/Cellar/'):
                if 'gradle' in i:
                    gradle_path = '/usr/local/Cellar/' + i + '/' + os.listdir('/usr/local/Cellar/'+ i)[0] + \
                                    '/' + 'bin/gradle'

                    return gradle_path
                else:
                    pass
            if gradle_path == None:
                gradle_path = QFileDialog.getExistingDirectory(
                        self.dlg,
                        "Select gradle path",
                        expanduser("~"),
                        QFileDialog.ShowDirsOnly
                    )
                return  gradle_path                   
        else:
            print 'currently the plugin only supports Mac and Windows'
             

    def run(self):
        """Run method that performs all the real work"""

        # show the dialog
        self.dlg.show()

        # Run the dialog event loop
        result = self.dlg.exec_()

        # See if OK was pressed
        if result:
            gradle_command = self.get_gradle_dir()
            dc_directory = self.dlg.lineEdit.text()
            print dc_directory
            if dc_directory == '':
                self.iface.messageBar().pushMessage("Error", "Please provide the directory of the Digital Connector", level=QgsMessageBar.CRITICAL)
            else:
                dc_recipe = self.track_recipe_choice()
                to_save = self.select_output_name()

                # Update DC and QGIS repo
                if self.dlg.checkBox_2.isChecked():
                    git_path = None
                    if platform.system() == 'Windows':
                        # Look in both Program Files and Program Files x86
                        for i in os.listdir('C:\\Program Files'):
                            if 'Git' in i:
                                git_path = 'C:\\Program Files\\' + i + '\\bin'
                                # No single quotes allowed in the string on Windows...
                                output = sp.call('{0}\\git pull'.format(git_path), cwd=dc_directory)
                            else:
                                pass
                        for j in  os.listdir('C:\\Program Files (x86)'):
                            if 'Git' in j:
                                git_path = 'C:\\Program Files (x86)\\' + j + '\\bin'
                                output = sp.call('{0}\\git pull'.format(git_path), cwd=dc_directory)
                            else:
                                pass 
                        # If all fails ask user             
                        if git_path == None:
                            git_path = QFileDialog.getExistingDirectory(
                                    self.dlg,
                                    "Select git path",
                                    expanduser("~"),
                                    QFileDialog.ShowDirsOnly
                                )
                            output = sp.call('{0}\\git pull'.format(git_path), cwd=dc_directory)               
                    # git pull for Mac OSX
                    elif platform.system() == 'Darwin':
                        for i in os.listdir('/usr/local/Cellar/'):
                            if 'git' in i:
                                git_path = '/usr/local/Cellar/' + i + '/' + os.listdir('/usr/local/Cellar/'+ i)[0] + \
                                                '/' + 'bin/git'
                                args = ['{0} pull'.format(git_path)]
                                output = sp.Popen(args, stdout=sp.PIPE, cwd=dc_directory, shell=True)
                            else:
                                pass
                        if git_path == None:
                            git_path = QFileDialog.getExistingDirectory(
                                    self.dlg,
                                    "Select git path",
                                    expanduser("~"),
                                    QFileDialog.ShowDirsOnly
                                )
                            args = ['{0} pull'.format(git_path)]
                            output = sp.Popen(args, stdout=sp.PIPE, cwd=dc_directory, shell=True)
                    else:
                        print 'currently the plugin only supports Mac and Windows'                    


                # check if the path corresponds to the examples folder or not. 
                # This is necessary due to the absolute paths of subprocess
                chars = set("\/")
                if any((c in chars) for c in dc_recipe):
                    pass
                else:
                    dc_recipe = '{0}/src/main/resources/executions/examples/{1}'.format(dc_directory,dc_recipe,to_save)


                # Check for -Pclear=True
                if self.dlg.checkBox.isChecked():
                    p_clear = 'true'
                else:
                    p_clear= 'false'


                # TODO need to add more error messages
                if not to_save:
                    self.iface.messageBar().pushMessage("Error", "Please choose a name for the output file", level=QgsMessageBar.CRITICAL)
                else:
                    if platform.system() == 'Windows':
                        if not gradle_command in os.environ['PATH']:
                            os.environ['PATH'] += ';' + gradle_command
                        else:
                            pass
                            
                        # No single quotes allowed in the string on Windows...
                        output = sp.call('{0} runExport -Precipe="{2}"  -Poutput="{3}" -Pclear="{4}"'.format(gradle_command + '\\gradle.bat',
                                                                                        dc_directory,dc_recipe,to_save, p_clear),
                                                                                        cwd=dc_directory)
                        # Adding the resulting layer in the QGIS Layers Panel
                        vlayer = QgsVectorLayer(to_save,to_save.split("/")[-1],"ogr")
                        QgsMapLayerRegistry.instance().addMapLayer(vlayer)   
                    else:
                        args = ["{0} runExport -Precipe='{2}'  -Poutput='{3}' -Pclear='{4}'".format(gradle_command,dc_directory,
                                                                                            dc_recipe,to_save, p_clear)]
                        output = sp.Popen(args,stdout=sp.PIPE, cwd=dc_directory, shell=True)
                        for log in iter(output.stdout.readline, b''):
                            sys.stdout.write(str(log) + '\n')

                        progressbar = QProgressBar()
                        progressbar.setMinimum(0)
                        progressbar.setMaximum(0)
                        progressbar.setValue(0)
                        progressbar.setWindowTitle("Running gradle task...")
                        progressbar.show()


                        # Adding the resulting layer in the QGIS Layers Panel
                        vlayer = QgsVectorLayer(to_save,to_save.split("/")[-1],"ogr")
                        QgsMapLayerRegistry.instance().addMapLayer(vlayer)    

    def on_combobox_changed(self, value):
        recipes =  self.dlg.lineEdit.text() + "/src/main/resources/executions/examples"
        recipes_list = []
        for file in os.listdir(recipes):
            if file.endswith(".json"):
                recipes_list.append(file)
        dc_url = "https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/resources/executions/examples/"
        
        # Catch list index error if selecting a recipe that doesn't exist in resources/executions
        try:
            url_recipe = urlLink=" <a href=\"{1}{0}\"> <font face=verdana size=3 color=blue> {0}</font> </a>".format(recipes_list[value],dc_url)
        except IndexError, e:
            url_recipe = urlLink=" <a href=\"{0}\"> <font face=verdana size=3 color=blue> {0}</font> </a>".format(dc_url)
        
        self.dlg.label_4.setOpenExternalLinks(True)
        update_url_recipe = self.dlg.label_4.setText(url_recipe)
        print("combobox changed", value)

    def clean_json(self, file):
        """ Clean json from comments """

        with open(file) as f:
            content = f.readlines()

        jsn = ''    
        for i in content:
            if '//' in i:
                pass
            else:
                jsn = jsn+i.replace("/n",'')
        
        f = json.loads(jsn)
        return f


    def edit_recipe(self):
        """ Fires up load recipe class and keeps track of the edited result """
        # get thet recipe
        file = '{0}/src/main/resources/executions/examples/{1}'.format(self.dlg.lineEdit.text(),self.track_recipe_choice())
        recipe_file = self.clean_json(file)

        # fire up the edit window
        updated_recipe, result = EditRecipe.getRecipeContent(datasources = recipe_file["dataset"]["datasources"],
                                                        subjects = recipe_file["dataset"]["subjects"],
                                                        fields = recipe_file["dataset"]["fields"])
        
        # if it was edited add it to the combobox
        if result:
            dc_recipe = self.dlg.comboBox.addItem(updated_recipe)
        
        print updated_recipe

    def select_output_name(self):
        """Returns the name of the exported geojson"""
        
        name = QFileDialog.getSaveFileName(self.dlg, 
            'Save File')
        return name

    def track_recipe_choice(self):
        """ Tracks recipe choice from combobox """
        dc_recipe = str(self.dlg.comboBox.currentText())
        return dc_recipe

    def resolve(self, name, basepath=None):
        if not basepath:
            basepath = os.path.dirname(os.path.realpath(__file__))
        return os.path.join(basepath, name)

    # Visualing recipe
    def visualise_recipe(self):
        """ Visualing recipe using plantUML
            Install it using brew install plantuml
        """
        dc_directory = self.dlg.lineEdit.text()
        dc_recipe = self.track_recipe_choice()

        file  = '{0}/src/main/resources/executions/examples/{1}'.format(dc_directory,dc_recipe)
        try:
            # Check if graphviz is installed
            if platform.system() == 'Windows':
                graphviz_path = None
                # Look in both Program Files and Program Files x86
                for i in os.listdir('C:\\Program Files'):
                    if 'Graphviz' in i:
                        graphviz_path = 'C:\\Program Files\\' + i + '\\bin'
                    else:
                        pass

                if graphviz_path != None:
                    pass
                else:
                    for j in  os.listdir('C:\\Program Files (x86)'):
                        if 'Graphviz' in j:
                            graphviz_path = 'C:\\Program Files (x86)\\' + i + '\\bin'
                        else:
                            pass
                # ERROR cannot find graphviz installation
                if graphviz_path == None:
                    graphviz_path = QFileDialog.getExistingDirectory(
                            self.dlg,
                            "Select graphviz path",
                            expanduser("~"),
                            QFileDialog.ShowDirsOnly
                        )
                # Add missing PATHs for windows
                current_execs = os.environ['PATH']
                if not 'graphviz' in current_execs:
                    os.environ['PATH'] += ';' + graphviz_path    

            # check graphviz for Mac OSX             
            elif platform.system() == 'Darwin':
                for i in os.listdir('/usr/local/Cellar/'):
                    
                    if 'graphviz' in i:
                        graphviz_path = '/usr/local/Cellar/' + i + '/' + os.listdir('/usr/local/Cellar/'+ i)[0] + \
                                        '/' + 'bin'
                        print graphviz_path
                    else:
                        pass
                if graphviz_path == None:
                    graphviz_path = QFileDialog.getExistingDirectory(
                            self.dlg,
                            "Select graphviz path",
                            expanduser("~"),
                            QFileDialog.ShowDirsOnly
                        )
                # Add missing PATHs for windows
                current_execs = os.environ['PATH']
                if not 'graphviz' in current_execs:
                    os.environ['PATH'] += ':' + graphviz_path    
            else:
                self.iface.messageBar().pushMessage("Error", "We currently support only Windows and MacOSX", level=QgsMessageBar.CRITICAL)

            from graphviz import Digraph
            from graphviz import Source

            with open(file) as f:
                content = f.readlines()
            jsn = ''    
            for i in content:
                if '//' in i:
                    pass
                else:
                    jsn = jsn+i.replace("/n",'')
                    
                    
                
            f = json.loads(jsn)
            c, r = self.traverse(f, None)

            dot = Digraph()
            dot.attr('node', shape='box')

            for i in c:
                dot.node(i['clsNameFullReference'], label ='''<<table border="0">
                                                            <tr>
                                                                <td>{0}</td>
                                                            </tr>
                                                            <hr/>
                                                            <tr><td><FONT COLOR="red" POINT-SIZE="16.0">Properties</FONT></td></tr>
                                                            {1}
                                                            <hr/>
                                                            <tr><td><FONT COLOR="red" POINT-SIZE="16.0">Objects</FONT></td></tr>
                                                            {2}
                                                    </table>>'''.format(i['clsName'],
                                                                        ['<tr><td>'+j+'</td></tr>' for j in i['clsProperties']],
                                                                    ['<tr><td>'+k+'</td></tr>' for k in i['clsObjects']]))
                

            for j in r:
                dot.edge(j.split('-->')[0].split(" ")[0].replace('"',""),j.split('-->')[-1].split(" ")[-1].replace('"',""))  
            s = Source(dot, filename=expanduser("~") + "/"+"test.gv", format="png")
            s.view()
        except ImportError, e:

            self.iface.messageBar().pushMessage("Error", "You need to install graphviz to visualise the recipe. Please refer to installation instructions", level=QgsMessageBar.CRITICAL)

        # self.dict2svg(self.clean_json(file))
    

    def traverse(self, obj, parent):
        """Traverse the dictionary"""
        vertices = []
        edges = []

        for key, value in obj.items():

            properties = []
            children = {}
            children_str = []

            # The value is a list of things. Iterate over them and add
            # an object for each element. Add a child as well.
            if isinstance(value, list):

                for idx, a in enumerate(value):

                    (children_str
                        .append("{}: {}".format(idx, "[object Object]")))

                    if isinstance(a, dict):
                        children[idx] = a
                    else:
                        children[idx] = {"value [{}]".format(type(a).__name__): a}

                vertices.append({
                    "clsName": key,
                    "clsNameFullReference": key,
                    "clsProperties": properties,
                    "clsObjects": children_str
                })

            # The value is a dictionary.
            elif isinstance(value, dict):

                for k, v in value.items():

                    if isinstance(v, dict):
                        (children_str
                            .append("{}: {}".format(k, "[object Object]")))
                        children[k] = v
                    elif isinstance(v, list):
                        (children_str
                            .append("{}: {}".format(k, "[object Object]")))
                        children[k] = v
                    else:
                        properties.append("{}: {}".format(k, v))

                vertices.append({
                    "clsName": key,
                    "clsNameFullReference": key,
                    "clsProperties": properties,
                    "clsObjects": children_str
                })

            if children:
                p = "{}.{}".format(parent, key) if parent is not None else key

                tmpobj, tmprel = self.traverse(children, p)

                for r in tmprel:
                    edges.append(r)

                for o in tmpobj:
                    o["clsNameFullReference"] = \
                        "{}.{}".format(key, o["clsNameFullReference"])
                    vertices.append(o)

                for child in children:
                    if parent is not None:
                        (edges.append("\"{}.{}\" \"1\" --> \"1\" \"{}.{}.{}\""
                                    .format(parent, key, parent, key, child)))
                    else:
                        (edges.append("\"{}\" \"1\" --> \"1\" \"{}.{}\""
                                    .format(key, key, child)))

        return vertices, edges


    # def printClass(self, cls):
    #     """Returns the string reopresention of a class"""

    #     s = ""

    #     s += ("class \"{}\" as {} {{"
    #         .format(cls["clsName"], cls["clsNameFullReference"])) + "\n"

    #     s += "\t" + ".. Properties .." + "\n"
    #     if cls["clsProperties"]:
    #         for p in cls["clsProperties"]:
    #             s += "\t" + p + "\n"

    #     if cls["clsObjects"]:
    #         s += "\t" + ".. Objects .." + "\n"
    #         for o in cls["clsObjects"]:
    #             s += "\t" + o + "\n"

    #     s += "}" + "\n"

    #     return s


    # def dict2plantuml(self, d):
    #     """Covert a dictionary to PlantUML text
    #        In the future we might consider doing this 
    #        using graphviz
    #     """

    #     s = "@startuml\n"

    #     if isinstance(d, dict):
    #         d = {"root": d}

    #         c, r = self.traverse(d, None)

    #         for cls in c:
    #             s += self.printClass(cls) + "\n"

    #         for rel in r:
    #             s += rel + "\n"
    #     else:
    #         raise TypeError("The input should be a dictionary.")

    #     return s + "@enduml"

    
    # def plantuml_exec(self, *file_names):
    #     """Run PlantUML"""

    #     cmd = ["/usr/local/bin/plantuml",
    #         "-tpng"] + list(file_names)

    #     sp.check_call(cmd, shell=False, stderr=sp.STDOUT)

    #     return [os.path.splitext(f)[0] + ".png" for f in file_names]


    # def dict2svg(self, d):

    #     base_name = str(uuid.uuid4())
    #     uml_path = expanduser("~") + "/" + base_name + ".uml"

    #     with open(uml_path, 'w') as fp:
    #         fp.write(self.dict2plantuml(d))

    #     try:
    #         output = self.plantuml_exec(uml_path)
    #         svg_name = output[0]
    #         output = self.plantuml_exec(uml_path)
    #         svg_name = output[0]
    #         plt.imshow(mpimg.imread(svg_name))
    #         plt.show()

    #     finally:

    #         if os.path.exists(uml_path):
    #             os.unlink(uml_path)

    #         svg_path = base_name + ".png"
    #         if os.path.exists(svg_path):
    #             os.unlink(svg_path)