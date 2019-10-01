'''
  This recipe gets three datasources
     - London Air Quality data by calling the LAQNImporter.java
     - London Traffic data using TrafficCountImporter.java
     - London localAuthority data using OaImporter.java

  By using LatestValueField it performs an operation
    - LatestValue of 'NO2 40 ug/m3 as an annual mean' - 1
  
  By using ArithmeticField and LatestValueField if perform two operations
    - Sum of the mean of 'CarCountTaxis' - eq: 2
    - Sum of the mean of 'CountPedalCycles' - eq: 3
  
  Then for each localAuthority using GeographicAggregationField
    - It divides eq: 2 with eq: 3 to get 'BicycleFraction' - eq: 4
  
  It then writes eq: 1 and eq: 4 that are contained within a local aurhority using GeographicAggregationField

  To know more Digital Connector visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/README.md 
  and to know more about its entities like Subject, Attribute, Datasources, 
  please visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/recipe-language.md
'''

from os import path, pardir
import sys
sys.path.append(path.join(path.dirname(path.realpath(__file__)), pardir))

# importing the required classes for building the recipe
from recipe import Recipe, Dataset, Subject, Match_Rule, Datasource, GeographicAggregationField, LatestValueField, AttributeMatcher, ArithmeticField


# Creating Subject which has a scope of LocalAuthority and would filter the dataset for London only 
match_rule = Match_Rule(attribute_to_match_on='label', pattern='E090%')
main_subject = Subject(subject_type_label='localAuthority', provider_label='uk.gov.ons', match_rule=match_rule)

# Creating Datasources to tell DC which importer needs to be called 
# in order to dowload datasets required to run the recipe. For more info please refer to documentation
m_datasource_2 = Datasource(importer_class='uk.org.tombolo.importer.ons.OaImporter', datasource_id='localAuthority')
m_datasource_3 = Datasource(importer_class='uk.org.tombolo.importer.dft.TrafficCountImporter', datasource_id='trafficCounts', geography_scope=['London'])
m_datasource_4 = Datasource(importer_class='uk.org.tombolo.importer.lac.LAQNImporter', datasource_id='airQualityControl')

# Creating Fields
# There are two high level fields in this recipe, one is GeographicAggregationField (for NO2 data) and 
# other one is ArithmeticField for getting the fraction of BicycleCount and CarCount

# Here we are creating LatestValueField for NO2 40 ug/m3 to get the mean value of it 
# For that we are creating AttributeMatcher to tell DC what needs to be pulled from Database
# That AttributeMatcher object is then passed to the Field Object and then we are passing everything to 
# GeographicAggregationField
pf_subject = Subject(provider_label='erg.kcl.ac.uk', subject_type_label='airQualityControl')
attribute = AttributeMatcher(provider='erg.kcl.ac.uk', label='NO2 40 ug/m3 as an annual mean')
l_v_f = LatestValueField(attribute_matcher=attribute)
parent_field = GeographicAggregationField(subject=pf_subject, label='NitrogenDioxide', function='mean', field=l_v_f)

# Below we are creating 3 Fields 2 LatestValueFields and 1 ArithmeticField
# ArithmeticField is the parent field which contains two child fields (2 LatestValueFields)
# First LatestValueField is taking sum of all BicycleCount in a LocalAuthority
# Second LatestValueField is taking sum of all CarsTaxis in a LocalAuthority
# Then they both are passed to ArithmeticField which takes the 
# fraction of BicycleCount, with first field as divisor and second as dividend
a_sub = Subject(provider_label='uk.gov.dft', subject_type_label='trafficCounter')
attr = AttributeMatcher(provider='uk.gov.dft', label='CountPedalCycles')
sub_field_latest = LatestValueField(attribute_matcher=attr)
field_1 = GeographicAggregationField(label='BicycleCount', subject=a_sub, function='sum', field=sub_field_latest)
attr_2 = AttributeMatcher(provider='uk.gov.dft', label='CountCarsTaxis')
sub_field_latest_2 = LatestValueField(attribute_matcher=attr_2)
field_2 = GeographicAggregationField(label='CarCount', subject=a_sub, function='sum', field=sub_field_latest_2)
arithmetic_field = ArithmeticField(label='BicycleFraction', operation='div', operation_on_field_1=field_1, operation_on_field_2=field_2)

# Passing everything to a Dataset Object as a list. Building and running the recipe by 
# passing as arguments location of DigitalConnector and recipe output location. 
# The latter needs to be relative to user's home directory.
dataset = Dataset(subjects=[main_subject], datasources=[m_datasource_2, m_datasource_3, m_datasource_4], 
                fields=[parent_field, arithmetic_field])
recipe = Recipe(dataset=dataset)
recipe.build_recipe(output_location='Desktop/london-cycle-traffic-air-quality.json')
recipe.run_recipe(tombolo_path='Desktop/TomboloDigitalConnector', output_path='Desktop/london-cycle-traffic-air-quality.geojson')
                
