The Tombolo Digital Connector is a piece of software to combine urban datasets and urban models.

# Build

Build the Digital Connector with or without running the unit tests

```bash
gradle clean build copyDeps
gradle clean build copyDeps -x test
```

For Eclipse users the following command builds 

```bash
gradle cleanEclipse eclipse
```

# Quick start

## Create database and load initial fixtures

The following scripts will delete the existing tables in the tombolo database and create a new empty copy, wiht some initial fixtures.

```bash
createdb tombolo
psql -d tombolo < src/main/resources/sql/create_database.sql
psql -d tombolo < src/main/resources/sql/inital_fixtures.sql
```

## Load LSOA

```bash
sh scripts/loadLsoa.sh
```

## Load MSOA

```bash
sh scripts/loadMsoa.sh
```

## Load Local Authorities

```bash
sh scripts/loadLa.sh
```

### Set up test database

```bash
createdb tombolo_test
psql -d tombolo_test < src/main/resources/sql/create_database.sql
psql -d tombolo_test < src/test/resources/sql/initial_fixtures.sql
```

### Continuous Integration

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

# Example executions

Exports the London borough profiles form OrganiCity

```bash
gradle clean build copyDeps -x test
java -cp build/libs/TomboloDigitalConnector.jar:build/dependency-cache/* \
	uk.org.tombolo.DataExportRunner \
	src/main/resources/executions/organicity/export-borough-profiles.json \
	organicity-borough-profiles.json \
	false
cat organicity-borough-profiles.json | json_pp
```

# Useful database queries

This sections contain a number of useful database queries that should at some point be integrated into the connector main code

```bash
psql tombolo -c 'select subject_type_label, count(*) from subject group by subject_type_label'
```

Attributes
```bash
psql tombolo -c 'select provider_label, label, name from attribute'
```

Attribute and value count per subject type
```bash
psql tombolo -c 'select subject_type_label, count(distinct a.id), count(distinct value) as values from timed_value as tv left join subject as go on (tv.subject_id = go.id) left join attribute as a on (tv.attribute_id = a.id) group by subject_type_label'
```

Attribute and value count per subject type and provider
```bash
psql tombolo -c 'select subject_type_label, provider_label, count(distinct a.id), count(distinct value) as values from timed_value as tv left join subject as go on (tv.subject_id = go.id) left join attribute as a on (tv.attribute_id = a.id) group by subject_type_label, provider_label'
```

# Database geo unit transformation

This command might come handy when we start writing the data exporters

```bash
psql -d tombolo -c 'SELECT name, ST_AsGeoJSON(shape) from subject where limit 1'
```