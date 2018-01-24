# Frequently Asked Questions

- [General](#general)
- [Data](#data)
- [Fields](#fields)
- [Model recipes](#model-recipes)

## General

### What is the difference between the Tombolo project and Tombolo Digital Connector?
The Tombolo Digital Connector is one of the pieces of software developed within the Tombolo project. The Tombolo project has a much wider scope with, among others, work packages around city engagement and creating a network of practitioners of urban modellers.

### What is the difference between the Tombolo Digital Connector and a GIS system?
The Tombolo Digital Connector is a tool for downloading and combining urban data. It can be used to share data combination recipes. The Tombolo Digital Connector is not a data-storage system (although one of its components is a database where data is stored temporarily).

GIS systems are a more generic tools for manipulating data. The Tombolo Digital Connector can be used to download and combine datasets that can then be manipulated further in a GIS system.

### What is the difference between the Tombolo Digital Connector and data analytics tools?
The Tombolo Digital Connector is a tool for downloading and combining urban data. It can be used to share data combination recipes. The Tombolo Digital Connector is not a generic purpose data analytics or data visualisation tool. The Tombolo Digital Connector can be used to download and combine datasets that can then be manipulated further in a data analytics or data visualisation tools.

### Is it a middleware or API or platform?
It is a tool for joining heterogeneous datasets together.

## Data

### Does Tombolo provide me with data?
No, it provides you a tool to get the data.

### Can the digital connector solve the problem of missing values?
No, but you can write and importer for it or introduce a back-off field.

### What is the difference between fixed values and timed values?
In the [centralised data-format](Local-Datastore.md), subjects can be assigned two types of attributes: fixed and timed
. Fixed attributes are the ones whose values do not have a time associated with them, e.g. name of a road segment, type of a road, category of an Open Street Map geography, etc. Timed attributes are the ones whose values have a time associated with them. e.g. number of deprived households in a certain LSOA according to the 2011 Census, NO concentration for an air quality sensor at 2pm on a certain day, etc.

## Fields

### What is the difference between a Field and an Attribute?

Attributes are used to represent characteristics of Subjects as they are imported from the external datasources. Fields are used to represent the data that we want to export. In the simplest form a Field will simply specify which attribute we would like to export (see Value Fields below). However, in most cases the output is likely to be a more complicated, such as an arithmetic calculation over multiple attributes (see Transformation Fields below), aggregation of attribute values from fine grained geometries to coarse grained geometries (see Aggregation Fields below), etc.

In short, Attributes represent data input and Fields represent data output.

## Model recipes

### What is the difference between a model and a recipe?

A recipe describes how to combine data to create a model. A model is the result of applying the recipe to a data-set.
