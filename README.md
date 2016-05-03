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

# Example executions

Exports the London borough profiles form OrganiCity

```bash
gradle clean build copyDeps -x test
java -cp build/libs/TomboloDigitalConnector.jar:build/dependency-cache/* \
	uk.org.tombolo.DataExportEngine \
	src/main/resources/executions/organicity/export-borough-profiles.json \
	organicity-borough-profiles.json \
	false
cat organicity-borough-profiles.json | json_pp
```

# Useful database queries

This sections contain a number of useful database queries that should at some point be integrated into the connector main code

```bash
psql tombolo -c 'select geography_type_label, count(*) from geography_object group by geography_type_label'
```

Attributes
```bash
psql tombolo -c 'select provider_label, label, name from attribute'
```

Attribute and value count per geography type
```bash
psql tombolo -c 'select geography_type_label, count(distinct a.id), count(distinct value) as values from timed_value as tv left join geography_object as go on (tv.geography_id = go.id) left join attribute as a on (tv.attribute_id = a.id) group by geography_type_label'
```

Attribute and value count per geography type and provider
```bash
psql tombolo -c 'select geography_type_label, provider_label, count(distinct a.id), count(distinct value) as values from timed_value as tv left join geography_object as go on (tv.geography_id = go.id) left join attribute as a on (tv.attribute_id = a.id) group by geography_type_label, provider_label'
```

# Database geo unit transformation

This command might come handy when we start writing the data exporters

```bash
psql -d tombolo -c 'SELECT name, ST_AsGeoJSON(shape) from geography_object where limit 1'
```