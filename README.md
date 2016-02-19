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

<pre>
wget 'https://geoportal.statistics.gov.uk/Docs/Boundaries/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip' -O /tmp/lsoa.zip
</pre>
<pre>
mkdir /tmp/lsoa
</pre>
<pre>
unzip /tmp/lsoa.zip -d /tmp/lsoa
</pre>
<pre>
shp2pgsql /tmp/lsoa/LSOA_2011_EW_BGC_V2.shp lsoa | psql -d tombolo
</pre>
<pre>
psql -d tombolo -c 'insert into area(area_type_id, label, name, shape) select '1', lsoa11cd, lsoa11nm, geom from lsoa'
</pre>
<pre>
psql -d tombolo -c 'drop table lsoa'
</pre>
<pre>
rm -r /tmp/lsoa
</pre>
<pre>
rm /tmp/lsoa.zip
</pre>

## Load MSOA

<pre>
wget 'https://geoportal.statistics.gov.uk/Docs/Boundaries/Middle_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip' -O /tmp/msoa.zip
</pre>
<pre>
mkdir /tmp/msoa
</pre>
<pre>
unzip /tmp/msoa.zip -d /tmp/msoa
</pre>
<pre>
shp2pgsql /tmp/msoa/MSOA_2011_EW_BGC_V2.shp msoa | psql -d tombolo
</pre>
<pre>
psql -d tombolo -c 'insert into area(area_type_id, label, name, shape) select '2', msoa11cd, msoa11nm, geom from msoa'
</pre>
<pre>
psql -d tombolo -c 'drop table msoa'
</pre>
<pre>
rm -r /tmp/msoa
</pre>
<pre>
rm /tmp/msoa.zip
</pre>

## Load Local Authorities

<pre>
wget 'https://data.gov.uk/dataset/local-authority-district-gb-dec-2012-boundaries-full-extent/datapackage.zip' -O /tmp/la.zip
</pre>
<pre>
mkdir /tmp/la
</pre>
<pre>
unzip /tmp/la.zip -d /tmp/la
</pre>
<pre>
unzip '/tmp/la/data/Local_authority_district_(GB)_Dec_2012_Boundaries_(Full_Extent).zip' -d /tmp/la/data
</pre>
<pre>
shp2pgsql /tmp/la/data/LAD_DEC_2012_GB_BFE.shp la | psql -d tombolo
</pre>
<pre>
psql -d tombolo -c 'insert into area(area_type_id, label, name, shape) select '3', lad12cd, lad12nm, geom from la'
</pre>
<pre>
psql -d tombolo -c 'drop table la'
</pre>
<pre>
rm -r /tmp/la
</pre>
<pre>
rm /tmp/la.zip
</pre>

# Database geo unit transformation

<pre>
psql -d tombolo -c 'SELECT ST_AsGeoJSON(ST_Transform(ST_SETSRID(shape, 27700),4326)) from area where area_type_id = 2 limit 1'
</pre>