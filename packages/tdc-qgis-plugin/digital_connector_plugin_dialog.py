# -*- coding: utf-8 -*-
"""
/***************************************************************************
 DigitalConnectorPluginDialog
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

import os

from PyQt4 import QtGui, uic, QtCore
import json
from FrameLayout import FrameLayout
import ast
import collections
import json
from pygments import highlight
from pygments.lexers import JsonLexer
from pygments.formatters import HtmlFormatter

FORM_CLASS, _ = uic.loadUiType(os.path.join(
    os.path.dirname(__file__), 'digital_connector_plugin_dialog_base.ui'))

class DigitalConnectorPluginDialog(QtGui.QDialog, FORM_CLASS):
    def __init__(self, parent=None):
        """Constructor."""
        super(DigitalConnectorPluginDialog, self).__init__(parent)
        # Set up the user interface from Designer.
        # After setupUI you can access any designer object by doing
        # self.<objectname>, and you can use autoconnect slots - see
        # http://qt-project.org/doc/qt-4.8/designer-using-a-ui-file.html
        # #widgets-and-dialogs-with-auto-connect
        self.setupUi(self)

class EditRecipe(QtGui.QDialog):
    def __init__(self, parent = None):
        super(EditRecipe, self).__init__(parent)
        
        self.layout = QtGui.QVBoxLayout(self)
        self.framesubjects = FrameLayout(title="Subjects")
        self.framedatasources = FrameLayout(title="Datasources")
        self.framefields = FrameLayout(title="Fields")

        # self.layout = QtGui.QGridLayout(self)

        # OK and Cancel buttons
        buttons = QtGui.QDialogButtonBox(
            QtGui.QDialogButtonBox.Save | QtGui.QDialogButtonBox.Cancel,
            QtCore.Qt.Horizontal, self)
        buttons.accepted.connect(self.accept)
        buttons.rejected.connect(self.reject)
        self.layout.addWidget(buttons)
    
    def addContentDatasources(self, datasource):
        widget = QtGui.QTextEdit()
        # widget.setText(datasource)

        # Example of how to use colocoding?
        json_object = json.loads(str(datasource).replace("'",'"'))
        json_str = json.dumps(json_object, indent=4, sort_keys=True)
        widget.setText((highlight(json_str, JsonLexer(), HtmlFormatter(noclasses=True))))

        self.framedatasources.addWidget(widget)
        return self.layout.addWidget(self.framedatasources)

    def addContentSubjects(self, subjects):
        widget = QtGui.QTextEdit()
        # widget.setText(subjects)

        json_object = json.loads(str(subjects).replace("'",'"'))
        json_str = json.dumps(json_object, indent=4, sort_keys=True)
        widget.setText((highlight(json_str, JsonLexer(), HtmlFormatter(noclasses=True))))

        self.framesubjects.addWidget(widget)
        return self.layout.addWidget(self.framesubjects)

    def addContentFields(self, fields):
        widget = QtGui.QTextEdit()
        # widget.setText(fields)

        json_object = json.loads(str(fields).replace("'",'"'))
        json_str = json.dumps(json_object, indent=4, sort_keys=True)
        widget.setText((highlight(json_str, JsonLexer(), HtmlFormatter(noclasses=True))))

        self.framefields.addWidget(widget)
        return self.layout.addWidget(self.framefields)

    def nested_dict_iter(self, d):
        stack = d.items()
        while stack:
            k, v = stack.pop()
            if isinstance(v, dict):
                print(k)
                stack.extend(v.iteritems())
            else:
                print("%s: %s" % (k, v))
        
    # def addDatasourceLabel(self):
    #     widget = QtGui.QLabel()
    #     widget.setText("Recipe's datasources")
    #     return self.layout.addWidget(widget)

    # def addSubjectsLabel(self):
    #     widget = QtGui.QLabel()
    #     widget.setText("Recipe's subjects")
    #     return self.layout.addWidget(widget)

    def getContentDatasources(self):
        updated_content = []
        # items = (self.layout.itemAt(i) for i in range(self.layout.count())) 
        items =  self.framedatasources.getWidget()
        for i in items:
            if isinstance(i.widget(), QtGui.QTextEdit):
                updated_content.append(i.widget().toPlainText())
        return updated_content

    def getContentFields(self):
        updated_content = []
        items =  self.framefields.getWidget()
        for i in items:
            if isinstance(i.widget(), QtGui.QTextEdit):
                updated_content.append(i.widget().toPlainText())
        return updated_content

    def getContentSubjects(self):
        updated_content = []
        items =  self.framesubjects.getWidget()
        for i in items:
            if isinstance(i.widget(), QtGui.QTextEdit):
                updated_content.append(i.widget().toPlainText())
        return updated_content


    # static method that reads the recipe content and returns the updated recipe
    @staticmethod
    def getRecipeContent(datasources,subjects,fields, parent = None):

        dialog = EditRecipe(parent)
        
        for i, j in enumerate(subjects):
            dialog.addContentSubjects(json.dumps(j,indent=4, sort_keys=True))
        for i, j in enumerate(datasources):
            dialog.addContentDatasources(json.dumps(j,indent=4, sort_keys=True))
        for i, j in enumerate(fields):
            dialog.addContentFields(json.dumps(j,indent=4, sort_keys=True))

        # dialog.addSubjectsLabel()
        # dialog.addDatasourceLabel()



        # execute the window and check whether Save was pushed
        result = dialog.exec_()

        if result:
            # get the updated contents
            updated_datasources = dialog.getContentDatasources()
            updated_fields = dialog.getContentFields()
            updated_subjects = dialog.getContentSubjects()

            # remove pretty print 
            updated_datasources = [i.replace("\n","").replace(" ","") for i in updated_datasources]
            updated_fields = [i.replace("\n","").replace(" ","") for i in updated_fields]
            updated_subjects = [i.replace("\n","").replace(" ","") for i in updated_subjects]

            updated_recipe = ( '{"dataset": {'
                '"subjects": ['+','.join(str(e) for e in updated_subjects)+'],'
                '"datasources": ['+','.join(str(e) for e in updated_datasources)+'],'
                '"fields": ['+','.join(str(e) for e in updated_fields)+']'
                '},'
                '"exporter": "uk.org.tombolo.exporter.GeoJsonExporter"}'
            )

            # make a prompt dialog box for savinhg the edited recipe
            output_recipe = QtGui.QFileDialog.getSaveFileName(dialog,  'Save File')

            if output_recipe == '':
                pass
            else:
                # save it and keep track of the file path
                with open(output_recipe, 'w') as outfile:
                    outfile.write(updated_recipe)

            # updated_recipe = updated_subjects + updated_datasources + updated_fields
            # updated_recipe = ','.join(str(e) for e in updated_recipe)
        else:
            output_recipe = []

        return output_recipe, result