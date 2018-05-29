'''
  This recipe gets two datasources
     - London Traffic data using TrafficCountImporter.java only for London
     - London localAuthority data using OaImporter.java
  
  Then for each localAuthority using GeographicAggregationField
    - It sums all the bicycle count based on LocalAuthority
  
  It then sums the individual bicycle counts that are contained within a local aurhority using GeographicAggregationField

  To know more Digital Connector visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/README.md 
  and to know more about its entities like Subject, Attribute, Datasources, 
  please visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/recipe-language.md
'''
from os import path, pardir
import sys
sys.path.append(path.join(path.dirname(path.realpath(__file__)), pardir))

# Importing all the relevant objects which are necessary
from recipe import Dataset, Subject, AttributeMatcher, GeographicAggregationField, LatestValueField, Match_Rule, Datasource, Recipe

# Creating match rule for London
match_rule = Match_Rule(attribute_to_match_on='label', pattern='E090%')
subject = Subject(subject_type_label='localAuthority', provider_label='uk.gov.ons', match_rule=match_rule)

# Creating datasource to tell DC which importers to call in order to download dataset
datasource_1 = Datasource(importer_class='uk.org.tombolo.importer.ons.OaImporter', datasource_id='localAuthority')
datasource_2 = Datasource(importer_class='uk.org.tombolo.importer.dft.TrafficCountImporter', 
                        datasource_id= 'trafficCounts', geography_scope=["London"])

# Creating Attribute matcher, which means getting only those values from database where 
# attribute name is CountPedalCycles
attribute_matcher = AttributeMatcher(provider='uk.gov.dft', label='CountPedalCycles')
field = LatestValueField(attribute_matcher=attribute_matcher, label='CountPedalCycles')

# Creating Subject for Geographic aggregation field
subject_2 = Subject(subject_type_label='trafficCounter', provider_label='uk.gov.dft')
geo_agg_field = GeographicAggregationField(subject=subject_2, field=field, function='sum', label='SumCountPedalCycles')

# Creating the dataset and calling DC to run the recipe
dataset = Dataset(subjects=[subject], datasources=[datasource_1, datasource_2], fields=[geo_agg_field])
_recipe = Recipe(dataset=dataset)
_recipe.build_recipe(output_location='Desktop/aggregate-traffic-count-data-within-localauthorities.json')
_recipe.run_recipe(tombolo_path='Desktop/UptodateProject/TomboloDigitalConnector', output_path='Desktop/aggregate-traffic-count-data-within-localauthorities.geojson')


