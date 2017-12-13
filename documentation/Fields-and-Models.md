# Fields and Models

In this section we describe the Tombolo Digital Connector fields and how modelling can be seen as fields. As with importers we have a set of built-in fields, recipes and models, together with a field specification language where users can define their own custom fields and models.

See also FAQ about [fields](Frequently-Asked-Questions.md#fields) and [model recipes](Frequently-Asked-Questions.md#model-recipes).

## Value Fields

Value fields are the most basic fields. Their purpose is to being able to export existing raw imported data values or to feed them to one of the nested fields introduced later on in this section.

Currently we have implemented the following value fields:

- **Fixed Annotation Field:** Returns a fixed value for annotation purposes.
- **Fixed Value Field:** Returns the Fixed Value of a specified Attribute for a given Subject.
- **Latest Value Field:** Returns the latest Timed Value for a particular Attribute on the given Subject.
- **Subject Latitude Field:** Returns the latitude of the centroid of the Subject.
- **Subject Longitude Field:** Returns the longitude of the centroid of the Subject.
- **Subject Name Field:** Returns the name of the Subject.
- **Values By Time Field:** Returns all Timed Values on an Attribute for a given Subject.

For an up-to-date list, see here: [value fields](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/field/value).

## Transformation Fields
Transformation fields take as input a set of one or more fields and produce a new field by transforming the values of the input fields.

- **Arithmetic Field:** Takes as input an operation, and two fields. It returns for a given Subject the value resulting from applying the operation on the two field values. Supported operations are addition, subtraction, multiplication and division.
- **Percentiles Field:** Field that returns for a subject and an attribute the percentile in which its value falls, compared to all the values for the same attribute for a set of subjects. Percentiles can be calculated either over the output subjects or any other specified set of subjects. E.g. we could output the quartiles value for the population density of all LSOAs in Leeds where the quartiles boundaries are calculated using population density values for all LSOAs in England.
- **Field Value Sum Field:** Takes a list of fields as input and returns a field consisting of the sum of the input fields (To be replaced by generalised Arithmetic Field).
- **Fraction Of Total Field:** For a subject, returns the sum of its Timed Values for a list of dividend attributes divided by a divisor attribute (To be replaced by generalised Arithmetic Field).

For an up-to-date list, see here: [transformation fields](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/field/transformation).

## Aggregation and Disaggregation Fields
The aggregation and disaggregation fields are types of transformation fields where the transformation is not only a combination of other fields but also either aggregates values from fine granularities to a coarse granularity or de-aggregates values from coarse granularities to finer granularities.

- **Geographic Aggregation Field:** A field that calculates the value of a field for every other Subject within its geometry and performs some aggregation function on the result. Currently we support sum and mean value aggregations. E.g. a cycle-traffic value for a local authority can be calculated by averaging the cycle-counts from all traffic counters located within that local authority.
- **Map To Containing Subject Field:** This field will find a subject of a given type that contains the provided subject, and then associate a field value with that new subject. For example, if the containing Subject Type is a 'City' and it is given a subject representing a building, it will assign the containing city’s field value to the building subject.
- **Map To Nearest Subject Field:** A field that finds the nearest subject of a given Subject Type and then evaluate the field specification with that new subject. For example, if the nearest Subject Type is 'Street' and it is given a subject representing a building, it will evaluate the field specification with a subject representing the Street that building is on.

For an up-to-date list, see here: [aggregation fields](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/java/uk/org/tombolo/field/aggregation).

## Modelling Fields and Built-in Models 
Modelling Fields and Built-in models are an important part of the Tombolo Digital Connector. It allows users to share definitions of custom fields and models. They are also used to encode built-in models that have been developed within the Tombolo project to address the City Challenges.
Basic Modelling Field: A field that takes as input a specification (recipe) of a potentially complex field and returns a value that is calculated according to the specification. The recipe/specification can also be seen as a model.

At the time of writing we include the following Built-in models:

- **[Active Transport Index](Use-Case-on-Active-Transport-Index.md):** A modelling field combining traffic counts 
from Department for Transport, cycling 
infrastructure from Open Street Map and travel to work information from the UK Census.
- **Social Isolation Score:** A modelling field for applying the Age UK model described in the city challenges description below.

These models are them selves combinations of sub models, e.g. for calculating the fraction of households renting in 
an LSOA. For browsing the available built-in models see [modelling fields](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/tree/master/src/main/resources/modelling-fields).