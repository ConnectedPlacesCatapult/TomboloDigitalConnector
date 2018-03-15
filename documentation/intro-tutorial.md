## Welcome to the Tombolo Digital Connector intro tutorial!

This tutorial is designed to help you get data out of the Tombolo Digital Connector as soon as possible. Here we assume
that you have set-up and installed the Tombolo Digital Connector using the instructions on our Github
[repo](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector).
If you haven’t yet, please ensure that you have installed the Tombolo Digital Connector by following the instructions
[here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector#quick-start). Our tutorials assume that you have
familiarity with the command line, installing software and managing permissions. If you are not familiar with these,
please do not worry. We’d like to know how useful our instructions are so try and give the installation a go and see
how far you get.

### Tutorial Sections

This tutorial has a number of sections that will help you to start making the most of the Tombolo Digital Connector. These sections are:
- [Opening a recipe template](#opening-a-recipe-template)
- [Finding the information to build a recipe](#finding-the-information-to-build-the-recipe)
- [Opening an importer file](#how-to-open-an-importer-file-in-digital-connector)
- [Writing a simple recipe](#writing-a-simple-json-recipe---no2-data-in-london)
- [Running a recipe](#run-the-recipe---no2-data-in-london)

Throughout this tutorial we will be discussing the notion of a ‘recipe’.


**A Recipe is: the set of instructions that tell the Tombolo Digital Connector what data to source, how to combine it
and how to export it.** **To learn more about the recipe language please see [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/recipe-language.md)**.

Please also note that we often include links to the Github repo when we refer to a particular part of the codebase. These links are designed to be examples and where appropriate, you should navigate to your locally cloned equivalent.

### Getting Started

There are three steps you need to take to get data from the digital connector, you will need to:
1. Create a recipe which specifies where the data is coming from and what you want to do with it.
2. Start the digital connector database server.
3. Run the recipe.

### Opening a Recipe Template

1. Open the digital connector folder (and associated files) in a text editor where you can see the file structure. We tend to use intelliJ or even Visual Studio Code, but any editor of your choice should work.
2. In your local clone of the Tombolo Digital Connector, navigate to the [template file](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/template.json).
3. Open the file called `template.json`.
4. Save template.json with a new name in the [examples folder](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/resources/executions/examples), the recipe can be saved anywhere but you will need to include the absolute file path when you run the recipe.

### Finding the information to build the recipe

The easiest way to learn about recipes is to have a look at the examples stored [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/resources/executions/examples). Try running them to see what
they do and once you’ve tested a few, see if you can generate an output that is useful to you. If it’s tricky, open a
 few and walk through them and read more about recipes [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/3b2ac1b161b59d437a1ca9a4ca70c041d97fc0af/documentation/recipe-language.md).

**To build a recipe you need information about the data you need and information about the operations you will be running on that data.**

The information to fill in the data portion of recipe can either be found in the importer files, which come with Tombolo, or perhaps more easily, in the `catalogue.json` file. We have provided an updated `catalogue.json` file alongside this tutorial and it can also be found here.
There are a number of `operations` you can perform on the data and these operations are known as fields. The available
fields in the can be found [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/field). The fields are
summarised [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/3b2ac1b161b59d437a1ca9a4ca70c041d97fc0af/documentation/fields-and-models.md).

If you do not have this catalogue file you can also use the Tombolo Digital Connector to generate it by doing the following:

1. Open a terminal window and navigate to the digital connector folder.
2. Run **gradle exportCatalogue -Poutput='catalogue.json'** (include a path to where you want the catalog to be saved
 if you don’t want to save it in the Tombolo Digital Connector Directory).
3. Drag and drop it on an empty browser window to see the json.

###How to open an importer file in Digital Connector

If you do not want to use the catalogue to populate your recipe, the importer files contain the information needed
about the data sources to create a recipe. All of these importers are stored in the [importer folder](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/importer), to open a file:
1. In your IDE navigate to the [importer folder](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/importer)
2. There is a folder for each available data provider. Open the relevant folder
3. Open the java importer file you require

### Writing a Simple JSON Recipe - NO2 data in London

There is a recipe in the examples folder that exports [NO2 data in London](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-no2.json), which you can check if you have any
problems writing the recipe below.

When you write a recipe you need to know what `data set(s)` you want and what `operation(s)` you would like to perform on that data. How you fill in the values in the recipes will depend on these two things.

As we mentioned earlier, the `data set(s)` will have an equivalent importer (also a java class) which is responsible for outlining how to get hold of the data. In this importer you can find some of the details needed to populate the recipe.

The values for the `operation(s)` on the data can be found [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/field). At this location, there are a number of folders
that contain java files for operations such as returning a basic value field or latest value field. There are also
folders that contain fields that can be used for arithmetic operations or even aggregation operations. For more
information about the different kinds of fields and an overview of what they do, please have a look [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/3b2ac1b161b59d437a1ca9a4ca70c041d97fc0af/documentation/fields-and-models.md).

The following example shows how you can get the data about NO2 levels in London. It uses the importer *LAQNImporter
.java* and the field file *LatestValueField.java*. An example of the completed recipe can be seen [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-no2.json).

1. Open the digital connector files in an interactive development environment (or text editor)
2. Either open the [LAQNImporter.java](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/java/uk/org/tombolo/importer/lac/LAQNImporter.java) importer file or find the equivalent data in the
`catalogue.json` file
3. Open the [template.json](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/template.json) file and save it with a new name.
4. Fill in the values for `subjectType` and `provider` in the subjects array:
   1. The value for `subjectType` is the value from the variable **LAQN_SUBJECT_TYPE_LABEL** in the *LAQNImporter.java*
   file. The value is `airQualityControl`
   2. The value for `provider` is the value from the variable **LAQN_PROVIDER_LABEL** in the *LAQNImporter.java* file.
    The value is `erg.kcl.ac.uk`
5. Fill in the values for `importerClass` and `datasourceId` in the datasources array:
    1. The value for `importerClass` is the name of the package plus the name of the file, without its extension. The
     package name is at the **top** of the *LAQNImporter.java* file preceded by the keyword **package**. The full value
     for the key `importerClass` is `uk.org.tombolo.importer.lac.LAQNImporter`
    2. The value for the `datasourceID` is the same as the subject type, `airQualityControl`
6. The `fieldClass` is the operation you want to perform on the data. Fill in the values for
`fieldClass` and the `label` in the fields array:
    1. The value of the field class is made up of the string: `uk.org.tombolo.field.value.`, plus the name of the
    field java file, without the extension. In this case the file is *LatestValueField.java*, so the `fieldClass`
    would be: `uk.org.tombolo.field.value.LatestValueField`
    2. In this case it would be `Annual NO2` , that’s user defined, if the label is top level that label will be in
    the output file. If the label is a child label, it won’t be seen.
7. Fill in the values in the `attribute` object in the fields array, there are two values in the `attribute` object, `provider` and `label`
    1. The value for the `provider` is found in the *LAQNImporter.java* file, it is the value of the variable
    *LAQN_PROVIDER_LABEL*, which is `erg.kcl.ac.uk`
    2. The value for the label is `NO2 40 ug/m3 as an annual mean` You can export to GeoJSON or CSV.
        1. If you want to export to GeoJSON then file in the exporter as `uk.org.tombolo.exporter.GeoJsonExporter`,
        and for CSV `uk.org.tombolo.exporter.CSVExporter`
To ensure that your recipe has been filled in successfully compare it with the [example](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-no2.json) in the code base. If that is
the case, you are ready to continue to the next step.

### Run the recipe - NO2 data in London

To run a recipe you need to go to the root folder of the digital connector and run two commands:

1. Make sure that the server is running. To start it on OSX in a console window type:
   ```bash
   pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
   ```
    You can do this at the user home directory
2. In a terminal window, navigate to the root of the digital connector and run:
   ```bash
   gradle runExport -Precipe='src/main/resources/executions/examples/london-no2.json' -Poutput='london-no2_output.json'
   ```
   Make sure that both lines of code are run at the same time, or you will get an error!
3. The export can take a few minutes. When it's ready, open the output file in a console window. At the root of the digital connector type the following:
   ```
   open london-no2_output.json
   ```

**Note:**

If you save your recipe in a different location you will need to use the *absolute path* when running the export.

**Note:**

If there is an error with the export you will get an output in your console window similar to the image below.


If you do get an error, check that both the `Precipe` and `Poutput` are run at the same time. When you copy and paste
 the code from Github it might be that only `Precipe` runs. The image below shows what you will see in the terminal
 if you haven’t put in the right run options.

![](https://user-images.githubusercontent.com/14051876/37464354-ff979de6-284f-11e8-867b-54d71b14ac06.jpg)

### Additional Notes / FAQ’s

**Notes on Installing the Digital Connector on a Mac:**

You have to have admin rights to install brew

Do I need to start the database server every time I want to use Tombolo?

No - it will likely keep running until you shut your computer down.

**Starting the database server**
```
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
```
   - You need to start the server in a place where you do have admin rights
   - If you have issues with this consult the start guides [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector#installation-guides).  
       When creating the databases (example below) you may need to be logged in as an administrator and you may need to
   be in the Tombolo Digital Connector folder.

**Exports**

Exports can take a few minutes to a few hours. If you are getting data for London, try to ensure that you use a match
 rule to exclude other parts of the country. Doing so can save a lot of time.

**Errors**

If you do get an error, check that both the **Precipe** and **Poutput** run together.

**Congratulations! You’ve used the Tombolo Digital Connector to access air quality data for London. Why not have a
look at some of our other [recipes](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/resources/executions/examples).**
