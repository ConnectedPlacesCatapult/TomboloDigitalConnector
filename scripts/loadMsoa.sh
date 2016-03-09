wget 'https://geoportal.statistics.gov.uk/Docs/Boundaries/Middle_layer_super_output_areas_(E+W)_2011_Boundaries_(Generalised_Clipped)_V2.zip' -O /tmp/msoa.zip
mkdir /tmp/msoa
unzip /tmp/msoa.zip -d /tmp/msoa
shp2pgsql /tmp/msoa/MSOA_2011_EW_BGC_V2.shp msoa | psql -d tombolo
psql -d tombolo -c "insert into geography_object(geography_type_label, label, name, shape) select 'msoa', msoa11cd, msoa11nm, geom from msoa"
psql -d tombolo -c 'drop table msoa'
rm -r /tmp/msoa
rm /tmp/msoa.zip