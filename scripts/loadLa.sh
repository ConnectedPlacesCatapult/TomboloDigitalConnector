wget 'https://data.gov.uk/dataset/local-authority-district-gb-dec-2012-boundaries-full-extent/datapackage.zip' -O /tmp/la.zip
mkdir /tmp/la
unzip /tmp/la.zip -d /tmp/la
unzip '/tmp/la/data/Local_authority_district_(GB)_Dec_2012_Boundaries_(Full_Extent).zip' -d /tmp/la/data
shp2pgsql /tmp/la/data/LAD_DEC_2012_GB_BFE.shp la | psql -d tombolo
psql -d tombolo -c "insert into geography_object(geography_type_label, label, name, shape) select 'localAuthority', lad12cd, lad12nm, geom from la"
psql -d tombolo -c 'drop table la'
rm -r /tmp/la
rm /tmp/la.zip