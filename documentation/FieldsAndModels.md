# Fields and models

In this section we describe the Tombolo Digital Connector fields and how modelling can be seen as fields. As with importers we have a set of built-in field recipes and modes, together with a field specification language where users can define their own custom fields and models.

## Value Fields

Value fields are the most basic fields. Their purpose is to being able to export existing raw imported data values or to feed them to one of the nested fields introduced later on in this section.


Currently we have implemented the following value fields:

- __Fixed Annotation Field:__ Returns a fixed value for annotation purposes.
- __Fixed Value Field:__ Returns the Fixed Value of a specified Attribute for a given Subject.
- __Latest Value Field:__ Returns the latest Timed Value for a particular Attribute on the given Subject.
- __Subject Latitude Field:__ Returns the latitude of the centroid of the Subject.
- __Subject Longitude Field:__ Returns the longitude of the centroid of the Subject.
- __Subject Name Field:__ Returns the name of the Subject.
- __Values By Time Field:__ Returns all Timed Values on an Attribute for a given Subject.

## Transformation Fields
Transformation fields take as input a set of one or more fields and produce a new field by transforming the values of the input fields.

- __Arithmetic Field:__ Takes as input an operation, and two fields. It returns for a given Subject the value resulting from applying the operation on the two field values. Supported operations are addition, subtraction, multiplication and division.
- __Percentiles Field:__ Field that returns for a subject and an attribute the percentile in which its value falls, compared to all the values for the same attribute for a set of subjects. Percentiles can be calculated either over the output subjects or any other specified set of subjects. E.g. we could output the quartiles value for the population density of all LSOAs in Leeds where the quartiles boundaries are calculated using population density values for all LSOAs in England.
- __Field Value Sum Field:__ Takes a list of fields as input and returns a field consisting of the sum of the input fields (To be replaced by generalised Arithmetic Field).
- __Fraction Of Total Field:__ For a subject, returns the sum of its Timed Values for a list of dividend attributes divided by a divisor attribute (To be replaced by generalised Arithmetic Field).

## Aggregation and Disaggregation Fields
The aggregation and disaggregation fields are types of transformation fields where the transformation is not only a combination of other fields but also either aggregates values from fine granularities to a coarse granularity or de-aggregates values from coarse granularities to finer granularities.

- __Geographic Aggregation Field:__ A field that calculates the value of a field for every other Subject within its geometry and performs some aggregation function on the result. Currently we support sum and mean value aggregations. E.g. a cycle-traffic value for a local authority can be calculated by averaging the cycle-counts from all traffic counters located within that local authority.
- __Map To Containing Subject Field:__ This field will find a subject of a given type that contains the provided subject, and then associate a field value with that new subject. For example, if the containing Subject Type is a 'City' and it is given a subject representing a building, it will assign the containing city’s field value to the building subject.
- __Map To Nearest Subject Field:__ A field that finds the nearest subject of a given Subject Type and then evaluate the field specification with that new subject. For example, if the nearest Subject Type is 'Street' and it is given a subject representing a building, it will evaluate the field specification with a subject representing the Street that building is on.

## Modelling Fields and Built-in Models 
Modelling Fields and Built-in models are an important part of the Tombolo Digital Connector. It allows users to share definitions of custom fields and models. They are also used to encode built-in models that have been developed within the Tombolo project to address the City Challenges.
Basic Modelling Field: A field that takes as input a specification (recipe) of a potentially complex field and returns a value that is calculated according to the specification. The recipe/specification can also be seen as a model.

At the time of writing we include the following Built-in models:

- __Social Isolation Among The Elderly:__ A modelling field for applying the Age UK model described in the city challenges description below.
- __Community Resilience:__ Work is underway to implement the outcomes of the community resilience collaboration with Leeds City Council into the Digital Connector. So-far we have built-in models/indices for:
  - __Disability:__ A modelling field for estimating disability resilience in an area.
  - __Overcrowding:__ A modelling field for estimating overcrowding resilience in an area.
  - __Combined Community Resilience:__ A modelling field combining the above mentioned resilience indices.

As we are in an active collaboration with Leeds City Council on defining community resilience indices we will be adding a wide range of community resilience models/indices to the system over the coming weeks.