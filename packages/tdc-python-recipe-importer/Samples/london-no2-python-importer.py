'''
The recipe is same as london-no2.py, the only difference is that 
instead of using uk.org.tombolo.importer.lac.LAQNImporter, it uses an 
importer built in python and first executes it and then passes the recipe 
to the Digital Connector.

To know more Digital Connector visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/README.md 
and to know more about its entities like Subject, Attribute, Datasources, 
please visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/recipe-language.md
'''
from os import path, pardir
import sys
sys.path.append(path.join(path.dirname(path.realpath(__file__)), pardir))

from recipe import Recipe, Field, Datasource, AttributeMatcher, Subject, Match_Rule, LatestValueField, Dataset

from Importers import importer_london_air_quality
subjects = Subject(subject_type_label='airQualityControl', provider_label='erg.kcl.ac.uk')
datasources = Datasource(importer_class='', datasource_id='')

attribute_matcher = AttributeMatcher(provider='erg.kcl.ac.uk', label='NO2 40 ug/m3 as an annual mean')
lvf = LatestValueField(attribute_matcher=attribute_matcher, label='NO2 Value')

dataset = Dataset(subjects=[subjects], datasources=[datasources], fields=[lvf])
recipe = Recipe(dataset=dataset)
recipe.build_recipe(output_location='Desktop/london-no2-python-importer.json')
recipe.run_recipe(tombolo_path='Desktop/UptodateProject/TomboloDigitalConnector', output_path='Desktop/london-no2-python-importer.geojson')