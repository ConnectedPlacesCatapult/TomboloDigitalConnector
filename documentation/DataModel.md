# Tombolo Digital Connector Data Model

At the core of the Digital Connector there is a data-model for urban data. In the current implementation the data is stored in a PostGIS database instance.

__Provider__ is a data object representing sources of data. A provider could be a governmental source of data, such as ONS; or a private but publicly available data source, such as Twitter feeds. In the future we may extend it to represent internal processes for aggregating or manipulating data, such as a process that aggregates age distribution data from the ONS Census and generates statistics on the fraction of the population that is 65 or older.

A provider consists of:

- _label:_ a unique label for the provider
- _name:_ a name of the provider


__Subject__ is a data object representing any type of subject or entity, which can include geographic objects, individuals and businesses.

A subject consists of:

- _label:_ a unique label for the subject.
- _name:_ a name of the subject.
- _subject type:_ type of the subject (e.g. point, street segment, building, lsoa, msoa, local authority, individual, business, etc.)
- _geometry:_ in case the subject has an associated geometry


__Attribute__ is a data object representing anything that could be measured or calculated, such as population density, CO2 concentration, obesity rate, etc. 

An attribute consists of:

- _provider:_ refers to the organisation or source of the data values for this attribute.
- _label:_ a label of the attribute, unique for each provider
- _name:_ name of the attribute (e.g. Population density, Obesity rate, CO2 concentration, etc.)
- _data-type:_ datatype of the attribute (e.g. integer, float, string, etc.)


__Fixed Value__ is a fixed attribute that can be assigned to a Subject. Examples can be a road-type attribute assigned to a road Subject.

A fixed value consists of:

- _subject:_ a foreign key reference to a Subject
- _attribute:_ a foreign key reference to an Attribute
- _value:_ the actual value, most often a string.


__Timed Value__ is a data object representing the value of an attribute for a certain subject, taken at a certain time point.

A timed value consists of:

- _subject:_ a foreign key reference to a Subject
- _attribute:_ a foreign key reference to an Attribute
- _timestamp:_ time point (or interval) to which the value refers (e.g. 2015-03-04T10:33:44Z, 2015, March 2015, etc.)
- _value:_ the actual value, most often a numeric value
