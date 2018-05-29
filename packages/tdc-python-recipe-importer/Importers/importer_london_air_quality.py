'''
This importer is replication of LAQNImporter of DigitalConnector.
This importer is built as an example of how to build importers using 
Importer.py.
This importer has also been used in an example recipe london-no2-python-importer.py.
'''

from os import path, pardir
import sys
sys.path.append(path.join(path.dirname(path.realpath(__file__)), pardir))

import convert_to_dataframe
import pandas as pd
import numpy as np
import json


data_url = 'http://api.erg.kcl.ac.uk/AirQuality/Annual/MonitoringObjective/GroupName=London/Year=2012/json'

from Utils import Utils
data = Utils.download_data(data_url=data_url, suffix='json')

from Importer import Provider, SubjectType, AbstractImporter, home_dir, Attribute, Subject, Geometry, FixedValue, TimedValue
importer = AbstractImporter(tombolo_path='/Desktop/UptodateProject/TomboloDigitalConnector/')

# Creating Provider
provider = Provider(label='erg.kcl.ac.uk', name='Environmental Research Group Kings College London')

# Creating SubjectType
subject_type = SubjectType(provider=provider, label='erg.kcl.ac.uk', name='airQualityControl')

# Getting data
to_dataframe = convert_to_dataframe.convert(data=data)
data = pd.DataFrame(data=to_dataframe)

# Create Attributes
attributes = []
create_attr = data['SiteObjectives.Site.Objective.@SpeciesCode'] + ' ' + data['SiteObjectives.Site.Objective.@ObjectiveName']
attr_labels = (create_attr.unique()).tolist()
attr_labels.extend(data.columns.values)
for a in attr_labels:
    attr = Attribute(provider=provider, label=a, description=a)
    attributes.append(attr)

# Create Subjects
subjects = []
cols = ['SiteObjectives.Site.@SiteCode', 'SiteObjectives.Site.@SiteName','SiteObjectives.Site.@SiteType', 
        'SiteObjectives.Site.@Latitude', 'SiteObjectives.Site.@Longitude','SiteObjectives.Site.@LatitudeWGS84',
        'SiteObjectives.Site.@LongitudeWGS84','SiteObjectives.Site.@SiteLink', 'SiteObjectives.Site.@DataOwner',
        'SiteObjectives.Site.@DataManager']

# print(type(data[cols[0]].tolist()))
sub_frame = pd.DataFrame([data[cols[0]].tolist(), data[cols[1]].tolist(), 
                        data[cols[2]].tolist(), data[cols[3]].tolist(), data[cols[4]].tolist(), 
                        data[cols[5]].tolist(), data[cols[6]].tolist(), data[cols[7]].tolist(),
                        data[cols[8]].tolist(), data[cols[9]].tolist()])
sub_frame = sub_frame.T
sub_frame.columns = cols
to_iter = sub_frame.groupby(['SiteObjectives.Site.@SiteCode'], as_index=False).first()
for index, value in to_iter.iterrows():
    geo = Geometry(latitude=str(value['SiteObjectives.Site.@Latitude']), longitude=str(value['SiteObjectives.Site.@Longitude']))
    s = Subject(subject_type=subject_type, label=value['SiteObjectives.Site.@SiteCode'], name=str(value['SiteObjectives.Site.@SiteName']), 
                shape=geo)
    subjects.append(s)

# Get Fixed Values
fixed_value = []
for i, sub in enumerate(subjects):
    for attr in attributes:
        if attr._label in cols:
            f = FixedValue(sub, attr, (to_iter.loc[[i]][attr._label]).values[0])
            fixed_value.append(f)

# Get Timed Values
timed_values = []
for i, values in data.iterrows():
    for attr in attributes:
        if attr._label.startswith(values['SiteObjectives.Site.Objective.@SpeciesCode']) and \
            attr._description == values['SiteObjectives.Site.Objective.@SpeciesCode'] + ' ' + values['SiteObjectives.Site.Objective.@ObjectiveName']:
            temp_sub = None
            for s in subjects:
                if s._label == values['SiteObjectives.Site.@SiteCode']:
                    temp_sub = s
                    break
            t = TimedValue(subject=temp_sub, attribute=attr, timestamp=values['SiteObjectives.Site.Objective.@Year'],
                            value=values['SiteObjectives.Site.Objective.@Value'])
            
            timed_values.append(t)

# save all values
importer.save(fixed_values=fixed_value, timed_values=timed_values)


