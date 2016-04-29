set -e -x

#wget 'https://data.gov.uk/dataset/local-authority-district-gb-dec-2012-boundaries-full-extent/datapackage.zip' -O /tmp/la.zip
wget 'https://data.gov.uk/dataset/county-and-unitary-authorities-ew-2012-boundaries-full-extent/datapackage.zip' -O /tmp/la.zip
mkdir /tmp/la
unzip /tmp/la.zip -d /tmp/la
unzip '/tmp/la/data/County_and_unitary_authorities_(E+W)_2012_Boundaries_(Full_Extent).zip' -d /tmp/la/data
shp2pgsql /tmp/la/data/CTYUA_DEC_2012_EW_BFE.shp la | psql -d tombolo
psql -d tombolo -c "insert into geography_object(geography_type_label, label, name, shape) select 'localAuthority', ctyua12cd, ctyua12nm, ST_Transform(ST_SETSRID(geom, 27700),4326) from la"
psql -d tombolo -c 'drop table la'
rm -r /tmp/la
rm /tmp/la.zip