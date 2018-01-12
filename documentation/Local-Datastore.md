# Local Datastore

At the core of the Digital Connector there is a centralised data-format for urban data. We refer to the physical implementation of the data-format as the Local Datastore. In the current implementation the data is stored in a PostGIS database instance. Figure 1 shows the Entity Relationship Diagram for the Local Datastore.

![Entity Relationship Diagram](https://user-images.githubusercontent.com/14051876/33561211-f8d7412c-d909-11e7-9785-10fa91dae980.png)

***Figure 1:*** *ERD for the Tombolo Digital Connector centralised data-format (Local Datastore).*

In short, the datastore contains a collection of **subjects** (e.g. traffic-counters, road segments or neighbourhoods) and **attributes** (e.g. bicycle-count, population, etc.). For each subject and each attribute, there is associated a time-series of zero or more values. For attributes that do not change over time we also have the notion of fixed values (e.g. road names).

Below we describe the centralised data format in more detail. Database field names are written in italics and primary keys as boldface.

## Provider
**Provider** is a data object representing sources of data. A provider could be a governmental organisation, such as ONS; or a private entity supplying a publicly available data source, such as Twitter.

A provider consists of:

- ***label:*** a unique label for the provider
- *name:* a name of the provider

## Subject Type
**Subject Type** is the type of subjects imported into the system (see description of subjects below). Subject types can be various such as traffic counter, IoT sensor, street segment, building, lsoa, msoa, local authority, etc.

A subject type consists of:

- ***provider:*** a provider of the subject type. I.e., the organisation responsible for defining and providing digital version of the subject.
- ***label:*** a label for the subject type. The label is unique for each provider.
- *name*: a human readable name of the subject type.

## Subject
**Subject** is a data object representing any type of subject or entity, which will in most cases refer to geographic objects, although we could support other subject types such as individuals or businesses. We support a both physical geographic subjects, such as street segments and buildings, as well as logical geographic subjects such as a geo-coded tweet or image.

A subject consists of:

- ***subject type:*** type of the subject (See description above).
- ***label:*** a unique label for the subject. The labels are unique within a subject type.
- *name:* a human readable name of the subject.
- *shape:* the geometric shape of the subject, in case it has one.

## Attribute
**Attribute** is a data object representing anything that could be measured or calculated, such as population density (for an LSOA), CO2 concentration (for an air quality sensor), obesity rate (for a local authority), etc. 

An attribute consists of:

- ***provider:*** refers to the organisation or source of the data values for this attribute.
- ***label:*** a label of the attribute, unique for each provider.
- *name:* a human readable name of the attribute (e.g. Population density, Obesity rate, CO2 concentration, etc.)
- *description:* a further description of the attribute, e.g. describing the methodology used to generate the attribute values.

## Fixed Value
**Fixed Value** is a fixed attribute that can be assigned to a Subject. Examples can be a the value of a road-type Attribute assigned to a road Subject.

A fixed value consists of:

- ***subject:*** a foreign key reference to a Subject.
- ***attribute:*** a foreign key reference to an Attribute.
- *value:* the actual value of the attribute for the subject, most often a string.

## Timed Value
**Timed Value** is a data object representing the value of an attribute for a certain subject, taken at a certain time point.

A timed value consists of:

- ***subject:*** a foreign key reference to a Subject.
- ***attribute:*** a foreign key reference to an Attribute.
- ***timestamp:*** time point to which the value refers. (E.g. 2015-03-04T10:33:44Z)
- *value:* the actual value for the attribute, subject and timestamp, most often a numeric value.
