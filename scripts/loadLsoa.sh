set -e -x

wget 'https://geoportal.statistics.gov.uk/Docs/Boundaries/Lower_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip' -O /tmp/lsoa.zip
mkdir /tmp/lsoa
unzip /tmp/lsoa.zip -d /tmp/lsoa
shp2pgsql -W LATIN1 /tmp/lsoa/LSOA_2011_EW_BGC_V2.shp lsoa | psql -d tombolo
psql -d tombolo -c "insert into subject_object(subject_type_label, label, name, shape) select 'lsoa', lsoa11cd, lsoa11nm, ST_Transform(ST_SETSRID(geom, 27700),4326) from lsoa"
psql -d tombolo -c 'drop table lsoa'
rm -r /tmp/lsoa
rm /tmp/lsoa.zip