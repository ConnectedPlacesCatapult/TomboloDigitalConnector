# Tombolo Digital Connector
[![wercker status](https://app.wercker.com/status/2279bdc90688501386b12c693be6a186/s/master "wercker status")](https://app.wercker.com/project/byKey/2279bdc90688501386b12c693be6a186)

The Tombolo Digital Connector is a software library for integrating urban models and datasets.

For further information see the [wiki](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/wiki).

## Table of Contents:

* [Quick start](#quick-start)
* [Continuous Integration](#continuous-integration)
* [Local Deploy](#local-deploy)
* [Run Tasks](#run-tasks)
* [Wiki to PDF](#wiki-to-pdf)

<p align="center">
  <img src="/readmeresources/dc_animation.gif?raw=true" alt="DigitalConnectorGif"/>
</p>

## Quick start

To get started you will need to install the requirements to run the Digital Connector. 

This tutorial will guide you to a quick start on Mac OS X. Installation tutorials for other operating systems will come 
soon.

<span style="color:red"> **Note: you’ll need to have administrator rights on your machine to install this - 
make sure that you do before you proceed.**
</span>

### Requirements

**Install the following** via the link through to their installation page:

* [Java Development Kit (1.8+)](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [PostgreSQL (9.4+)](https://www.postgresql.org/)
* [PostGIS (2.1+)](http://postgis.net/)
* [Gradle (2.12+)](https://gradle.org/)
* [Git](https://git-scm.com/download/mac)

#### A note about the Terminal

The [Terminal](https://en.wikipedia.org/wiki/Terminal_(macOS)) application can be found in the Applications -> Utilities folder or quickly accessed through 
Spotlight. It is pre-installed in the MacOS X so there is no need to install it.

You will need this application to run some of the commands of this tutorial. When you enter a command and press 
return/enter, the terminal will execute it and complete the task.

**Make sure to press return after typing a command before you enter the next one.**

#### Let's start

* Open the **Terminal**. All the following steps will operate in it.

* Check if you have installed the right versions for the requirements by entering each of the following commands in 
the Terminal.

  ```bash
  java -version
  psql --version
  gradle --version
  git --version
  ```

  The output will look something like this:

  ```bash
  $ java -version
  java version "1.8.0_121"
  Java(TM) SE Runtime Environment (build 1.8.0_121-b13)
  Java HotSpot(TM) 64-Bit Server VM (build 25.121-b13, mixed mode)
  $ psql --version
  psql (PostgreSQL) 9.6.3
  $ gradle --version
  
  ------------------------------------------------------------
  Gradle 3.4
  ------------------------------------------------------------
  
  Build time:   2017-02-20 14:49:26 UTC
  Revision:     73f32d68824582945f5ac1810600e8d87794c3d4
  
  Groovy:       2.4.7
  Ant:          Apache Ant(TM) version 1.9.6 compiled on June 29 2015
  JVM:          1.8.0_121 (Oracle Corporation 25.121-b13)
  OS:           Mac OS X 10.11.6 x86_64
  
  $ git --version
  git version 2.10.1 (Apple Git-78)
  ```

* Get the Digital Connector code to your local machine by cloning its repository.

  ```bash
  git clone https://github.com/FutureCitiesCatapult/TomboloDigitalConnector
  ```

  If successful, you will see a log similar to the below.

  ```bash
  $git clone https://github.com/FutureCitiesCatapult/TomboloDigitalConnector  
  Cloning into 'TomboloDigitalConnector'...
  remote: Counting objects: 15761, done.
  remote: Compressing objects: 100% (184/184), done.
  remote: Total 15761 (delta 90), reused 193 (delta 49), pack-reused 15487
  Receiving objects: 100% (15761/15761), 178.89 MiB | 3.04 MiB/s, done.
  Resolving deltas: 100% (7647/7647), done.
  ```

* Go to the Digital Connetor root directory and rename the properties files. These can be done you running each of the
following commands and pressing enter.

  ```bash
  cd TomboloDigitalConnector
  mv gradle.properties.example gradle.properties
  mv apikeys.properties.example apikeys.properties
  ```

  The previous commands will allow you to use the default project settings.  

  *If you prefer/need you can amend the settings altering the default ones to the ones you decide.*


### Set up database

The following step sets up a main and a test database after starting the server.
The test database is used by the tests and is cleared routinely. We use this to gain control over what is in the 
database when our tests are running and to avoid affecting any important data in your main database.

```bash
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
chmod +x create_db.sh
./create_db.sh
```

*For more information or to change the default database and user settings access the file [create_db.sh](create_db.sh).*

### Run tests 

A quick check on how everything has been set up is to run all the tests. If they are successful, it will mean that 
everything went fine.

Run the command in the Terminal.

```bash
gradle test
```

If successful the output will be as the following.

```bash
$ gradle test
:compileJava UP-TO-DATE
:processResources UP-TO-DATE
:classes UP-TO-DATE
:compileTestJava UP-TO-DATE
:processTestResources UP-TO-DATE
:testClasses UP-TO-DATE
> Building 85% > :test > 50 tests completed
:test

BUILD SUCCESSFUL

Total time: 4 mins 50.919 secs
```

**If the tests start to fail then check the PostgreSQL server is running and the requirements are properly installed by
 going through the previous steps.**
 
About to be mentioned a couple of examples of what might have gone wrong in the process if the tests start failing.

```bash
uk.org.tombolo.core.AttributeTest > testUniqueLabel FAILED
    java.util.ServiceConfigurationError
        Caused by: org.hibernate.service.spi.ServiceException
            Caused by: org.hibernate.exception.JDBCConnectionException
                Caused by: org.postgresql.util.PSQLException
                    Caused by: java.net.ConnectException

uk.org.tombolo.core.AttributeTest > testWriteJSON FAILED
    java.util.ServiceConfigurationError
        Caused by: org.hibernate.service.spi.ServiceException
            Caused by: org.hibernate.exception.JDBCConnectionException
                Caused by: org.postgresql.util.PSQLException
                    Caused by: java.net.ConnectException

uk.org.tombolo.core.DatasourceTest > testWriteJSON FAILED
    java.util.ServiceConfigurationError
        Caused by: org.hibernate.service.spi.ServiceException
            Caused by: org.hibernate.exception.JDBCConnectionException
                Caused by: org.postgresql.util.PSQLException
                    Caused by: java.net.ConnectException
```

The former error log is launched if the server is not running and to solve it you need to run the command.

```bash
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
```

In case you see this other error instead, it means that you did not rename the settings files successfully.

```bash
FAILURE: Build failed with an exception.

* Where:
Build file '/TomboloDigitalConnector/build.gradle' line: 159

* What went wrong:
Execution failed for task ':test'.
> Test environment not configured. See the README.
```

If you see other errors, try to go back and follow the steps again.


### Run the Digital Connector

Now you are all set to run a task on the Digital Connector.

The next step is to run an example to show how the digital connector combines different data sets.
We’re using an example that shows the relationship between air pollution (demonstrated in this example by NO2 levels), 
and car and bicycle traffic in every borough in London. You can read more about this example 
[here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/wiki/Tutorial).  

When you’ve run this example, you can expect a map that looks like this: 

![Final Output](/readmeresources/final_output.png)

##### To get started:

+ Run the following command into the Terminal.

  ```bash
  gradle runExport \
    -PdataExportSpecFile='src/main/resources/executions/examples/london-cycle-traffic-air-quality-lsoa-backoff.json' \
    -PoutputFile='/Desktop/london-cycle-traffic-air-quality-lsoa-backoff-output.json'
  ```

+ You can expect it to take around 1.5 minutes to generate the output, which will be saved in the Desktop.
Change the path in the command in case you want it saved elsewhere.

  The output will look similar to the next content:
  
  ```json
  {
    "type":"FeatureCollection",
    "features":[
      {
        "type":"Feature",
        "geometry":{
          "type":"Polygon",
          "coordinates":[[[-0.0802,51.5069],[-0.1092,51.5099],[-0.1114,51.5098],
                          [-0.1116,51.5153],[-0.1053,51.5185],[-0.0852,51.5203],
                          [-0.0784,51.5215],[-0.0802,51.5069]]]
        },
        "properties":{
          "label":"E09000001",
          "name":"City of London",
          "Nitrogen Dioxide":81.3333333333333,
          "Bicycle Fraction":0.25473695591455 
        }
      }, ...
      ...
    ]
  }
  ```
  
+ Once you have your output, you can open with a geospatial visualisation tool. For this example, we recommend QGIS,
 which you can download [here](http://www.qgis.org/en/site/forusers/download.html).


### Open with QGIS

+ Install and open QGis
 
  Click [here](http://www.qgis.org/en/site/forusers/download.html) to download and choose the option relevant to your 
  operating 
  system.
 
+ Open the Digital Connector output in QGis by following these steps:
 
  + Click on **Layer -> Add Layer -> Add Vector Layer -> Browse (Choose the output file) -> Open**
  
    Now your screen should look something like this
 
    ![Raw Output File](/readmeresources/raw_output.png)
 
  + Change the Projection Type by clicking the button next to Render at the bottom of QGis
 
    ![Projection Options](/readmeresources/projection_option.png)
 
    Now you should see Project Properties Windows
 
    ![Projection Window](/readmeresources/projection_window.png)
 
    In the Filter box type 27700 and choose British National Grid
 
    ![Projection Search](/readmeresources/projection_search.png)
 
    Click OK, and now your screen should look something like this
 
    ![Raw Output with UK Projection](/readmeresources/raw_output_uk_projection.png)
 
  + On left side under Layers Panel, Right Click on your filename layer which in this tutorial is “qgis_tutorial 
   OGRGeoJSON Polygon” and choose Properties
 
      ![Layers Panel](/readmeresources/layers_panel.png)
 
      Now you see a Layer Properties Windows, something like this
 
      ![Layers Properties](/readmeresources/layer_properties.png)
 
      From top where it says “Single Symbol” choose “Categorized” and now your window should look like this
 
      ![Layers Properties with Catagorized Option](/readmeresources/layer_properties_catagorized.png)
 
       In Column choose “BicycleFraction”. In Color ramp choose “Blues” and then click on “Classify” Button and on the 
     pop up click “OK” and now your window should look something like this
 
     ![Layers Properties with options selected](/readmeresources/layer_properties_final_options.png)
 
     Click OK and Now your map should look something like this
 
     ![Output without map in the background](/readmeresources/output_without_bg_map.png)
 
   + Now click on Plugins -> Manage and Install Plugins
 
     ![Plugins menu](/readmeresources/plugin_option_menu.png)
 
     Plugin windows should appear. In case option “All” is not click and select it
 
     ![Plugins Window](/readmeresources/plugins_window.png)
 
     Search for QuickMapServices and then click on Install Plugin. Once the plugin is installed click Close
 
     ![Plugins Search Window](/readmeresources/plugins_search.png)
 
     Now you should have “Globe” like icon on your toolbar
 
     ![Quick Map Service Icon](/readmeresources/quick_map_services_icon.png)
 
     Click the Globe with + and Choose OSM -> OSM Standard
 
     ![Quick Map Service Options](/readmeresources/osm_menu_option.png)
 
     Now your map should look like this
 
     ![Final Output](/readmeresources/final_output.png)
 
   + Now if you want to zoom in, click the Magnifying glass with + on first toolbar
 
     ![Magnifying Buttons](/readmeresources/magnifying_buttons.png)
 
     Or change the Magnifier percentage on the bottom bar
 
     ![Magnifier Options](/readmeresources/magnifier_option.png)
 
   + Now to save as an image Go to Project -> Save as Image
 
    ![Save as Png](/readmeresources/save_as_image.png)
 
    Give it a name of your choice and save it in your preferred directory. 
    Now your png file should look like this
 
    ![Output in PNG](/readmeresources/output_in_png.png)





















### Run tests

```bash
gradle test
```

If you use the IntelliJ JUnit test runner, you will need to add the following to your
VM Options in your JUnit configuration (Run -> Edit Configurations -> All under JUnit,
and Defaults -> JUnit):

```
-enableassertions
-disableassertions:org.geotools...
-Denvironment=test
-DdatabaseURI=jdbc:postgresql://localhost:5432/tombolo_test
-DdatabaseUsername=tombolo_test
-DdatabasePassword=tombolo_test
```

## Local deploy

To deploy to your local Maven installation (`~/.m2` by default):

```
gradle install
```

## Run Tasks

### Run export

We use the Gradle task `runExport` to run exports. The parameters are as follows:

```bash
gradle runExport \
    -PdataExportSpecFile='path/to/spec/file.json' \
    -PoutputFile='output_file.json' \
    -PforceImports='com.className'
    -PclearDatabaseCache=true
```

For example, this calculates the proportion of cycle traffic received at a traffic counter relative to the total traffic
in a given borough and outputs the results to the file `reaggregate-traffic-count-to-la.json`:

```bash
gradle runExport \
    -PdataExportSpecFile='src/main/resources/executions/examples/reaggregate-traffic-count-to-la.json' \
    -PoutputFile='reaggregate-traffic-count-to-la_output.json'
```

### Export data catalogue

We use the Gradle task `exportCatalogue` to export a JSON file detailing the capabilities of the connector
and explore the data catalogue.

```bash
gradle exportCatalogue -PoutputFile=catalogue.json
```

## Continuous Integration

We're using [Wercker](http://wercker.com/) for CI. Commits and PRs will be run
against the CI server automatically. If you don't have access, you can use the
Wercker account in the 1Password Servers vault to add yourself.

If you need to run the CI environment locally:

1. Install the [Wercker CLI](http://wercker.com/cli/install)
2. Run `wercker build`

The base image is generated with the very simple Dockerfile in the root of this
project. To push a new image to DockerHub you will need access to our DockerHub
account. If you don't have access, you can use the DockerHub account in the
1Password Servers vault to add yourself.

If you need new versions of PostgreSQL, Java, etc, you can update the image:

```
docker build -t tombolo .
docker images
# Look for `tombolo` and note the IMAGE ID
docker tag <IMAGE_ID> fcclab/tombolo:latest
docker push fcclab/tombolo
```

## Wiki to PDF

To create a PDF version of the Wiki documentation clone the wiki respository 
and run the gradel build in the wiki repository root folder.

```
gradle build
```
