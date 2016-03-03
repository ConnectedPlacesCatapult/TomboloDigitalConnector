Warning! This is not a Readme ... This is a set of notes that should at some point be converted to a proper Readme.

# Build

<pre>
gradle cleanEclipse eclipse
</pre>

# Create database

<pre>
createdb tombolo
</pre>
<pre>
psql -d tombolo < src/main/resources/sql/create_database.sql
</pre>
<pre>
psql -d tombolo < src/main/resources/sql/inital_fixtures.sql 
</pre>

# Load data

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

<pre>
psql -d tombolo -c 'SELECT ST_AsGeoJSON(ST_Transform(ST_SETSRID(shape, 27700),4326)) from area where area_type_id = 2 limit 1'
</pre>