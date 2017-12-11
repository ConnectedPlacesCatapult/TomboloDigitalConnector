# Recipe language

The core means of using the Tombolo Digital Connector is to create a data export recipes, describing the data or models that we want to get out of the system, without describing how to generate the data. The recipe language is expressed in the well-known JSON format.

## Data export recipe
A data export recipe consists of two parts:

* **Dataset**: is a description of the data to be exported. The format of the dataset recipe is explained in the [Dataset recipe](#dataset-recipe) section below.
* **Exporter**: is the canonical name of the Java class to be used to export the data. At the time of writing there are two types of exporters, one for CSV output and one for GeoJson.

Example data export specification for GeoJson output:
```json
{
  "dataset" : INSERT-YOUR-DATASET-RECIPE,
  "exporter" : "uk.org.tombolo.exporter.GeoJsonExporter"
}
```

## Dataset recipe

A dataset recipe is composed of three parts:

* **Subjects**: Specifies the [Subjects](Local-Datastore.md#Subject) for which we return data in the final dataset. I
.e., return all LSOAs in Milton Keynes, return all traffic counters in Greenwich, or return all local authorities in England. See further details in the [Subject recipe](#subject-recipe) section.
* **Datasources**: A list of data-sources needed to be imported in order to export the data. As an example, data-sources can be the Space Syntax Open Space Map (SSx OSM) for the Royal Borough of Greenwich, traffic counts for London from Department for Transport (DFT), etc. See further details in the [Datasource recipe](#datasource-recipe) section.
* **Fields**: A list of [Fields](Fields-and-Models.md) that are to be returned for each subject. As an example, for a 
set of spatial network segments the user could specify to export the connectivity of each segment according to SSx OSM, the nearest DfT traffic counts for that segment (if available) and a deprivation value for that segment disaggregated from the LSOA level deprivation scores from Department for Communities and Local Government (DCLG). In case a field is a transformation or a modelling field, the needed computation is performed at the time of exporting. See further details in the [Field recipe](#field-recipe) section.

Example dataset specification skeleton:
```json
{
  "subjects" : [INSERT-SUBJECT-RECIPE],
  "datasources" : [INSERT-DATASOURCE-RECIPE],
  "fields" : [INSERT-FIELD-RECIPE]
}
```

## Subject recipe
A subject recipe is has two mandatory parts:

* **Provider**: specifies the provider of the subject subject to be returned.
* **Subject type**: specifies the type of the subject to be returned. At the time of writing we support various types, such as, local authority, msoa, lsoa, trafficCounter, SSxNode (a node in a Space Syntax graph), gpSurteries, etc.

Example of a simple subject specification where we return all traffic counters that have been imported.
```json
{
  "provider" : "uk.gov.dft",
  "subjectType" : "trafficCounter"
}
```

Additionally a subject recipe can have two types of so-called match rules where we can restrict further the set of Subjects returned:

* **match-rule**: is a filter where we can restrict the returned subjects based on name or label. The match-rule is further composed of:
  * **attribute**: is the attribute on which we would like to filter. E.g. name or label.
  * **pattern**: is a string pattern that the specified attribute should match. We use the SQL like syntax using % as the wildcard. E.g. for filtering all strings that start with the string 'Leeds' we use the pattern 'Leeds%'. 
* **geo-match-rule**: is a filter where we can restrict the returned subjects based on geographic constraints.

Example where we return all LSOAs whose name starts with the string 'Leeds':
```json
{
  "provider" : "uk.gov.ons",
  "subjectType" : "lsoa",
  "matchRule": {
    "attribute": "name",
    "pattern": "Leeds%"
  }
}
```

Example where we return all London boroughs based on filtering for the label prefix 'E090':
```json
{
  "provider" : "uk.gov.ons",
  "subjectType" : "localAuthority",
  "matchRule" : {
    "attribute": "label",
    "pattern": "E090%"
  }
}
```

Example where we return all traffic-counters in Greenwich and Islington:
```json
{
  "provider" : "uk.gov.dft",
  "subjectType" : "trafficCounter",
  "geoMatchRule" : {
    "geoRelation" : "within",
    "subjects" : [
      {
        "provider" : "uk.gov.ons",
        "subjectType" : "localAuthority",
        "matchRule" : {
          "attribute" : "name",
          "pattern" : "Greenwich"
        }
      },
      {
        "provider" : "uk.gov.ons",
        "subjectType" : "localAuthority",
        "matchRule" : {
          "attribute" : "name",
          "pattern" : "Islington"
        }
      }
    ]
  }
}
```

## Datasource recipe
The datasource recipe consists of two fields:

* **importer-class**: Which Java class is used to import the data.
* **datasource-id**: The identifier of the dataset to be imported. This value is unique within each importer class.

Example for LSOA geographies
```json
{
  "importerClass": "uk.org.tombolo.importer.ons.OaImporter",
  "datasourceId": "lsoa"
}
```

Examples for importing the ONS Census dataset for overcrowding:
```json
{
  "importerClass": "uk.org.tombolo.importer.ons.CensusImporter",
  "datasourceId": "QS408EW"
}
```

### Datasource geography scope

Some data providers support customisable downloading of their data based on geographic area. E.g. Department of Transport provides traffic counts for individual regions or local authorities.

* **geographyScope**: is a list of geographic areas to import. For the supported values see the code for the corresponding importer.

Example for importing DfT traffic-counts for London:
```json
{
  "importerClass" : "uk.org.tombolo.importer.dft.TrafficCountImporter",
  "datasourceId" : "trafficCounts",
  "geographyScope" : ["London"]
}
```

### Datasource temporal scope

Some datasources support configurable downloads for some time periods.

* **temporalScope**: is a list of temporal references for which data can be imported. For the supported values see the code for the corresponding importer.

## Field recipe
The field recipe contains a list of fields that you want to output. Fields can be as simple as the latest value for a certain attribute, such as population density, or a potentially complex model, such as one for estimating the risk of social isolation among the elderly. All fields have two default fields (sic):

* **fieldClass**: is the Java class used to calculate the field value
* **label**: the label that the recipe creator chooses to associate with the field

In addition each field has additional fields (sic) depending on the type of the field. For example a latest-value-field you need to specify the attribute for which you want the latest value. E.g.,

```json
"field": {
  "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
  "label": "Count of cars and taxis",
  "attribute": {
    "provider": "uk.gov.dft",
    "label": "CountCarsTaxis"
  }
}
```

As another example the arithmetic-field takes as an input the operation you want to apply and two fields 

```json
{
  "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
  "label": "An example of adding two fields",
  "operation": "add",
  "field1": INSERT-A-FIELD-RECIPE,
  "field2": INSERT-A-FIELD-RECIPE
}
```

Further examples can be found in the [Fields and Models](Fields-and-Models.md) description page.

## Notes

* Introduce two types of scope. 
  * Subject scope (optional)
  * Normalisation scope (optional)
