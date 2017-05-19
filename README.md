# Tombolo Digital Connector

The Tombolo Digital Connector is a software library for integrating urban models and datasets.

For further information see the [wiki](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/wiki).

## Table of Contents:

* [Quick start](#quick-start)
* [Continuous Integration](#continuous-integration)
* [Local Deploy](#local-deploy)
* [Run Tasks](#run-tasks)
* [Wiki to PDF](#wiki-to-pdf)

## Quick start

### Requirements
* JDK (1.8+)
* PostgreSQL (9.4+)
* PostGIS (2.1+)
* Gradle (2.12+)
* (Optional) Wercker (1.0+)

### Configure the project

Copy and amend the example configuration file at
`/gradle.properties.example` to
`/gradle.properties`.

Copy and amend the example API keys file at
`/apikeys.properties.example` to
`/apikeys.properties`. If you're not using the services mentioned in the file you can leave it as-is.

### Set up main database

Then run the following to set up your database:

```bash
# Create a user and database
createuser tombolo
createdb -O tombolo tombolo
psql -d tombolo -c "CREATE EXTENSION postgis;"

# Create DB tables and load initial fixtures
psql -d tombolo -U tombolo < src/main/resources/sql/create_database.sql
```

### Set up test database

The test database is used by the tests and is cleared routinely. We use this
to gain control over what is in the database when our tests are running and
to avoid affecting any important data in your main database.

To set up the test user and database:

```bash
# Create a user and database
createuser tombolo_test
createdb -O tombolo_test tombolo_test
psql -d tombolo_test -c "CREATE EXTENSION postgis;"

# Create DB tables and load initial fixtures
psql -d tombolo_test -U tombolo_test < src/main/resources/sql/create_database.sql
```

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
    -PoutputFile='reaggregate-traffic-count-to-la.json'
```

### Run data catalogue

We use the Gradle task `runCatalogue` to explore the data catalogue. The parameters are as follows:

```bash
gradle runCatalogue \
    -PimporterClassName='full.name.of.the.importer'
    -PdatasetId='dataset-id'
```

If the datasetId parameter is not present the program writes out all the datasets available from the importer.
If the datasetId is specificed the program writes out all attributes available from that importer dataset pair.

For example, this lists all datasets available in the ONS Census importer:

```bash
gradle runCatalogue -PimporterClassName='uk.org.tombolo.importer.ons.ONSCensusImporter'
```

For example, this lists all attributes available in the dataset QS102EW from ONS (Population density):

```bash
gradle runCatalogue -PimporterClassName='uk.org.tombolo.importer.ons.ONSCensusImporter' -PdatasetId='QS102EW'
```

### Export data catalogue

We us the Gradle task `exportCatalogue` to export a JSON file detailing the capabilities of the connector.

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
