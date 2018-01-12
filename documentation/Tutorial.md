# Tutorial - Cycling and Air Quality

In this page we will give a tutorial of how to use the Tombolo Digital Connector.

## Install system
First of all you should follow the 
[Quick Start](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector#quick-start) 
guide to get the digital connector up and running by installing requirements, configuring the project and setting up both the main and test databases.

## Export data
Having installed the system, it is time to run a data export recipe. In the 
[Use Case on Cycling and Air Quality](Use-Case-on-Cycling-and-Air-Quality.md) 
we described an example recipe where we output, for every borough in London, information about NO2 concentration and the ratio between the bicycle traffic and car traffic ([see recipe](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-cycle-traffic-air-quality.json)). To run the export recipe, run the following command from the root directory of the Tombolo Digital Connector:

```bash
gradle runExport \
  -PdataExportSpecFile='src/main/resources/executions/examples/london-cycle-traffic-air-quality.json' \
  -PoutputFile='london-cycle-traffic-air-quality-output.json'
```

As mentioned in the [use case description](Use-Case-on-Cycling-and-Air-Quality.md), this will give you a GeoJson file 
with the cycling and air quality indicators for each of the 33 London boroughs. Opening the Json file in QGIS and using a quantile-based colouring of the boroughs should give you an image similar to the one below ([See QGIS tutorial for reference](http://www.qgistutorials.com/en/docs/basic_vector_styling.html)).

![London borough cycle to car count ratio](https://user-images.githubusercontent.com/14051876/33561213-f9071faa-d909-11e7-98df-a8edae0c3a6a.png) 

## Change granularity
Suppose you want to get the cycling information at a finer granularity, say at [LSOA](Glossary#lsoa) level. Copy the data export recipe into a new file called `london-cycle-traffic-air-quality-lsoa.json`.

Change the subjects clause from outputting all 33 London boroughs to outputting all LSOA geographies that fall inside the 33 London boroughs. I.e., change:

```json
"subjects": [
  {
    "subjectType" : "localAuthority",
    "provider":"uk.gov.ons",
    "matchRule": {
      "attribute": "label",
      "pattern": "E090%"
    }
  }
]
```
to:

```json
"subjects": [
  {
    "subjectType": "lsoa",
    "provider": "uk.gov.ons",
    "geoMatchRule": {
      "geoRelation": "within",
      "subjects": [
        {
          "subjectType": "localAuthority",
          "provider": "uk.gov.ons",
          "matchRule": {
            "attribute": "label",
            "pattern": "E090%"
          }
        }
      ]
    }
  }
]
```

and add the following datasource to the list of datasources:

```json
{
  "importerClass" : "uk.org.tombolo.importer.ons.OaImporter",
  "datasourceId" : "lsoa"
}
```

What this does is that it tells the Digital Connector to output all LSOA geographies that fall inside the 33 London boroughs (We are here basing our work on the fact that London boroughs can be identified by a label staring with `E090`) ([see the full recipe](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-cycle-traffic-air-quality-lsoa.json)). Run the new recipe by executing the command:

```bash
gradle runExport \
  -PdataExportSpecFile='london-cycle-traffic-air-quality-lsoa.json' \
  -PoutputFile='london-cycle-traffic-air-quality-lsoa-output.json'
```

When looking at the output from the Digital Connector you will notice that you get very many warnings. This is because that there are very many LSOAs that do not have either a traffic counter in them or an air quality sensor, and hence there is no data to output. Yet, the Digital Connector does return a GeoJson file with the LSOA geographies. If you, again, use QGIS to create a quantile-based colouring of the LSOAs, you should get an image that looks like the one below. 

![London borough cycle to car count ratio](https://user-images.githubusercontent.com/14051876/33561215-f91ff0b6-d909-11e7-9d20-53e9c59ee0e5.png)

If you compare this image with the one from the first export you can see that it gives a finer granularity results, but the data is sparse due to the fact that not all LSOAs have a traffic counter. One way to overcome this sparseness of traffic counters and air quality sensors is to use a so-called back-off field. Back-off fields allow the user to output a different field value for a subject if none exists. In our case, if a traffic count value does not exist for an LSOA, we could instead output the traffic count value for the surrounding MSOA. If that value does not exist either, we could back-off to the local authority value.

Before we dive into back-off fields, it is better to introduce the the notion of modelling-fields.

## Modelling Fields
In the running example we have been using a combination of geographic aggregation fields and arithmetic fields to get the aggregated NO2 value for an area and the aggregated bicycle-to-car ratio for an area. These data aggregations and transformations are fairly basic and likely to be useful in city data analysis projects beyond this example. Hence we have wrapped them up as model recipes that can be used across different jobs without copying and pasting the entire code.

In order to demonstrate this, copy your lsoa data export recipe into a new file called `london-cycle-traffic-air-quality-lsoa-modelling.json`.

In this file you can replace the actual calculation with the corresponding pre-defined model recipe. I.e. replace: 

```json
{
  "fieldClass": "uk.org.tombolo.field.aggregation.GeographicAggregationField",
  "label": "NitrogenDioxide",
  "subject": {
    "provider": "erg.kcl.ac.uk",
    "subjectType": "airQualityControl"
  },
  "function": "mean",
  "field": {
    "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
    "attribute": {
      "provider" : "erg.kcl.ac.uk",
      "label" : "NO2 40 ug/m3 as an annual me"
    }
  }
}
```
with the corresponding pre-defined model of aggregating NO2:

```json
{
  "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
  "label": "NitrogenDioxide",
  "recipe": "environment/laqn-aggregated-yearly-no2"
}
```

Similarly the calculation of bicycle to car ratio:

```json
{
  "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
  "label": "BicycleFraction",
  "operation": "div",
  "field1": {
    ...        
  },
  "field2": {
    ...          
  }
}
```

Can be replaced by the corresponding pre-defined model:

```json
{
  "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
  "label": "BicycleFraction",
  "recipe": "transport/traffic-counts-aggregated-bicycles-to-cars-ratio",
  "datasources": [
    {
      "importerClass" : "uk.org.tombolo.importer.dft.TrafficCountImporter",
      "datasourceId" : "trafficCounts",
      "geographyScope" : ["London"]
    }
  ]
}
```

Note that in this case we additionally pass a list of datasources to the traffic counts model. This is because, by default, the model is set up for calculating the cycle to car ratio nationwide and will therefore import traffic counts for the entire country. However, since in this tutorial we are only interested in the data for London, we override the data-set import to only the London area.

This recipe is quite simpler than before ([see full recipe](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-cycle-traffic-air-quality-lsoa-modelling.json)). Now, run the new recipe by executing the command:

```bash
gradle runExport \
  -PdataExportSpecFile='london-cycle-traffic-air-quality-lsoa-modelling.json' \
  -PoutputFile='london-cycle-traffic-air-quality-lsoa-modelling-output.json'
```

If you look at the resulting file in QGIS, you will see that you get the same output as before but by utilising more simplified and re-usable code.

## Back-off Fields
Now that we have simplified the export recipe, we can go back to our intention to use back-off fields to overcome the sparseness of traffic counters and air quality sensors. A Back-off field takes as input an array of fields. It will try to calculate a value for each of the fields in order, and when it finds one it will output that one.

In order to demonstrate this, copy your modelling data export recipe into a new file called `london-cycle-traffic-air-quality-lsoa-backoff.json`.

Then replace the NO2 aggregation calculation:

```json
{
  "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
  "label": "NitrogenDioxide",
  "recipe": "environment/laqn-aggregated-yearly-no2"
}
``` 

with a back-off field that first tries to aggregate the NO2 values for the output geography (LSOAs). If no air quality sensor exists within the LSOA, it tries to aggregate NO2 values for the surrounding MSOA. If no air quality sensor exists within the MSOA, it will aggregate NO2 values for the surrounding local authority (borough). The resulting back-off field looks like this:

```json
{
  "fieldClass": "uk.org.tombolo.field.aggregation.BackOffField",
  "label": "NitrogenDioxide",
  "fields": [
    {
      "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
      "recipe": "environment/laqn-aggregated-yearly-no2"
    },
    {
      "fieldClass": "uk.org.tombolo.field.aggregation.MapToContainingSubjectField",
      "subject": {
        "provider": "uk.gov.ons",
        "subjectType": "msoa"
      },
      "field": {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "recipe": "environment/laqn-aggregated-yearly-no2"
      }
    },
    {
      "fieldClass": "uk.org.tombolo.field.aggregation.MapToContainingSubjectField",
      "subject": {
        "provider": "uk.gov.ons",
        "subjectType": "localAuthority"
      },
      "field": {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "recipe": "environment/laqn-aggregated-yearly-no2"
      }
    }
  ]
}
```

The back-off field for the ratio of bicycles to cars is exactly the same, only with changing the corresponding recipe ([see full recipe](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/examples/london-cycle-traffic-air-quality-lsoa-backoff.json)). As before you run the recipe with the command:

```bash
gradle runExport \
  -PdataExportSpecFile='london-cycle-traffic-air-quality-lsoa-backoff.json' \
  -PoutputFile='london-cycle-traffic-air-quality-lsoa-backoff-output.json'
```

Using QGIS to visualise the back-off model in a similar manner as done above, we get a much less sparse data output as shown in the image below.

![London borough cycle to car count ratio](https://user-images.githubusercontent.com/14051876/33561212-f8eef47a-d909-11e7-8009-645ac58da5fa.png)
