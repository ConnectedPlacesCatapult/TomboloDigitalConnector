Warning! This is not a proper Readme yet ... This is a set of notes that should at some point be converted to a proper Readme.

# Build

For Eclipse users the following command builds 

```bash
gradle cleanEclipse eclipse
```

# Quick start

## Create database and load initial fixtures

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

# Database geo unit transformation

This command might come handy when we start writing the data exporters

```bash
psql -d tombolo -c 'SELECT ST_AsGeoJSON(ST_Transform(ST_SETSRID(shape, 27700),4326)) from area where area_type_id = 2 limit 1'
```