'''
  This recipe gets two datasources
     - London Traffic data using TrafficCountImporter.java
     - London localAuthority data using OaImporter.java
  
  By using ArithmeticField and LatestValueField if perform two operations
    - Sum of the mean of 'CarCountTaxis' - eq: 1
    - Sum of the mean of 'CountPedalCycles' - eq: 2
  
  Then for each localAuthority using GeographicAggregationField
    - It divides eq: 2 with eq: 1 to get 'BicycleFraction' - eq: 3
  
  It then writes eq: 3 (BicycleFraction) that are contained within a local aurhority using GeographicAggregationField

  To know more Digital Connector visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/README.md 
  and to know more about its entities like Subject, Attribute, Datasources, 
  please visit https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/documentation/recipe-language.md
'''

from os import path, pardir
import sys
sys.path.append(path.join(path.dirname(path.realpath(__file__)), pardir))


from recipe import Recipe, Dataset, Subject, Match_Rule, Geo_Match_Rule, Datasource, BackOffField, SingleValueModellingField, MapToContainingSubjectField

# Creating match rule to filter on London Geography
match_rule = Match_Rule(attribute_to_match_on='label', pattern='E090%')
g_subject = Subject(subject_type_label='localAuthority', provider_label='uk.gov.ons', match_rule=match_rule)
geo_match_rule = Geo_Match_Rule(geo_relation='within', subjects=[g_subject])
main_subject = Subject(subject_type_label='lsoa', provider_label='uk.gov.ons', geo_match_rule=geo_match_rule)

# Creating datasources to download lsoa, msoa and localauthority geographies
m_datasource_1 = Datasource(importer_class='uk.org.tombolo.importer.ons.OaImporter', datasource_id='lsoa')
m_datasource_2 = Datasource(importer_class='uk.org.tombolo.importer.ons.OaImporter', datasource_id='msoa')
m_datasource_3 = Datasource(importer_class='uk.org.tombolo.importer.ons.OaImporter', datasource_id='localAuthority')

# referencing another recipe from modelling-fields/environment folder
# passing that field to an object of MapToContainingSubject field
s_v_m_f = SingleValueModellingField(recipe='environment/laqn-aggregated-yearly-no2')
subject_for_mtcsf = Subject(provider_label='uk.gov.ons', subject_type_label='msoa')
m_t_c_s_f = MapToContainingSubjectField(subject=subject_for_mtcsf, field=s_v_m_f)

# Creating another subject and MapToContainingSubject field and giving it SingleValueModellingField
subject_for_mtcsf_2 = Subject(provider_label='uk.gov.ons', subject_type_label='localAuthority')
m_t_c_s_f_2 = MapToContainingSubjectField(subject=subject_for_mtcsf_2, field=s_v_m_f)
# passing SingleValueModellingField and both MapToContainingSubjectField to BackOffField Object
back_off_field = BackOffField(fields=[s_v_m_f, m_t_c_s_f, m_t_c_s_f_2], label='NitrogenDioxide')

# Creating datasource for TrafficCountImporter to download traffic count data for London only
datasource = Datasource(importer_class='uk.org.tombolo.importer.dft.TrafficCountImporter', datasource_id='trafficCounts', geography_scope=['London'])
# Creating SingleValueModellingField by referencing another recipe from 
# modelling-fields/transport and giving it above created datasource 
s_v_m_f_with_datasource = SingleValueModellingField(recipe='transport/traffic-counts-aggregated-bicycles-to-cars-ratio', datasources=[datasource])

# Creating two MapToContainingSubjectField with above created mtcsf subjects and svmf_with_datasource as field
_m_t_c_s_f = MapToContainingSubjectField(subject=subject_for_mtcsf, field=s_v_m_f_with_datasource)
_m_t_c_s_f_2 = MapToContainingSubjectField(subject=subject_for_mtcsf_2, field=s_v_m_f_with_datasource)
back_off_field_2 = BackOffField(fields=[s_v_m_f_with_datasource, _m_t_c_s_f, _m_t_c_s_f_2], label='BicycleFraction')

# Creating a dataset object and running the recipe
dataset = Dataset(subjects=[main_subject], datasources=[m_datasource_1, m_datasource_2, m_datasource_3], 
                fields=[back_off_field, back_off_field_2])
recipe = Recipe(dataset=dataset)
recipe.build_recipe(output_location='Desktop/london-cycle-traffic-air-quality-lsoa-backoff.json')
recipe.run_recipe(tombolo_path='Desktop/UptodateProject/TomboloDigitalConnector', output_path='Desktop/london-cycle-traffic-air-quality-lsoa-backoff.geojson')
                
