'''
This recipe will get you started with Digital Connector, 
it uses the minimal objects required to build a useful recipe.

The recipe aims at giving the latest value of No2 40 ug/m3 annual mean 
for every LocalAuthority

To know more Digital Connector visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/README.md 
and to know more about its entities like Subject, Attribute, Datasources, 
please visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/recipe-language.md
'''

from os import path, pardir
import sys
sys.path.append(path.join(path.dirname(path.realpath(__file__)), pardir))

# Declaring the path of Digital Connector
# Declaring the path of the generated recipe, that would save the recipe in json format 
# before sending it to DC.
# Declaring the location of the outut file
# Note: all three declaration should be relative to user's home directory
tombolo_path = 'Desktop/TomboloDigitalConnector'
recipe_output_location = 'Desktop/london-no2.json'
model_output = 'Desktop/london-no2.geojson'

# importing the required classes for building the recipe
from recipe import Recipe, Field, Datasource, AttributeMatcher, Subject, Match_Rule, LatestValueField, Dataset

# Creating Subject and Datasources to tell DC which importer need to be called 
# in order to dowload datasets. For more info please refer to documentation
subjects = Subject(subject_type_label='airQualityControl', provider_label='erg.kcl.ac.uk')
datasources = Datasource(importer_class='uk.org.tombolo.importer.lac.LAQNImporter', datasource_id='airQualityControl')

# Creating Attribute to tell DC what exactly does it needs to search in the database
# Creating LatestValueField in order to get the Latest Record saved for NO2 40 mg for that year
# and passing it an object of Attribute matcher
attribute_matcher = AttributeMatcher(provider='erg.kcl.ac.uk', label='NO2 40 ug/m3 as an annual mean')
lvf = LatestValueField(attribute_matcher=attribute_matcher, label='Anual NO2')

# Passing everything to a Dataset Object as a list and building and running the recipe 
# in one single step
dataset = Dataset(subjects=[subjects], datasources=[datasources], fields=[lvf])
dataset.build_and_run(tombolo_path=tombolo_path, model_output_location=model_output, 
                        recipe_console_print=True)
