As part of the Tombolo project we have built an application called [City Index Explorer](https://labs.tombolo.org.uk/city-index-explorer/). The application functions as a demonstrator of the possibilities for combining various urban data sources into an urban model. One of the indices we have developed for demonstration is the Active Transport Index. The index combines various data sources to assign a score to each LSOA representing the use and potential for active transport.

In particular, the index consists or three components:
* **Cycle traffic**: The bicycle counts as a fraction of the total traffic count in the LSOA using traffic counter information from Department for Transport.
* **Cycle infrastructure**: The fraction of the entire road infrastructure that is fitted with cycling infrastructure (such as cycle lanes) using information from Open Street Map.
* **Active commute**: The fraction of total commuters that commute principally by either cycling or walking using information from the 2011 Census.

Below we will describe the generation of the index in detail, but see also [the model recipe for the active transport index](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/modelling-fields/city-indices/active-transport/ActiveTransportIndex-field.json) and [recipe for exporting as GeoJson the active transport index, together with its components for all LSOAs in England and Wales](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/city-indices/active-transport.json).

# Export recipe
As with other [export recipes](Recipe-Language), [the active transport index export recipe](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/executions/city-indices/active-transport.json) has 4 main parts.

* **subjects**: Tells the digital connector to calculate values for each [LSOA](Glossary#lsoa) in the UK.
* **datasources**: Tells which datasources need to be imported. In this case only the LSOAs, since any additional datasources needed by the index are covered by the recipe for the respective index or index component.
* **fields**: Tells the digital connector to export 4 fields: the index itself, together with its 3 components. The last 3 fields are not necessary when exporting the index, but for the [city index explorer application](https://labs.tombolo.org.uk/city-index-explorer/) we do need this information for data visualisation purposes.
* **exporter**: Tell the digital connector to export the data as GeoGson.

```JSON
{
  "dataset": {
    "subjects": [
      {
        "provider": "uk.gov.ons",
        "subjectType": "lsoa"
      }
    ],
    "datasources": [
      {
        "importerClass": "uk.org.tombolo.importer.ons.OaImporter",
        "datasourceId": "lsoa"
      }
    ],
    "fields": [
      {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "label": "index:active_transport",
        "recipe": "city-indices/active-transport/ActiveTransportIndex"
      },
      {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "label": "component:proportion_of_cycles_to_cars",
        "recipe": "city-indices/active-transport/CyclingCount-lsoa"
      },
      {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "label": "component:cycle_lane_score",
        "recipe": "city-indices/active-transport/CycleLaneCount-lsoa"
      },
      {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "label": "component:travel_to_work_score",
        "recipe": "city-indices/active-transport/ActiveTransportToWork"
      }
    ]
  },
  "exporter" : "uk.org.tombolo.exporter.GeoJsonExporter"
}
```

Below we will explain the active transport index and its components in more detail.

# Active Transport Index
As described above, the active transport index is composed of three components. The index is simply the sum of those three components, implemented as a list-arithmetic-field using the addition operation.

```JSON
{
  "fieldClass": "uk.org.tombolo.field.transformation.ListArithmeticField",
  "operation": "add",
  "fields": [
    {
      "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
      "recipe": "city-indices/active-transport/CyclingCount-lsoa"
    },
    {
      "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
      "recipe": "city-indices/active-transport/CycleLaneCount-lsoa"
    },
    {
      "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
      "recipe": "city-indices/active-transport/ActiveTransportToWork"
    }
  ]
}
```

In the future we might consider implementing the index as a "linear-combination-field". Pending implementation of that field.

Each component is described below. 

# Cycle traffic
The [cycle-traffic](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/modelling-fields/transport/traffic-counts-aggregated-bicycles-to-cars-ratio-field.json) component uses the Department for Transport traffic counts to calculated ratio between cycle traffic count and the sum of both cycle and car traffic counts. The field is implemented as a back-off-field where we first try to calculate a value for the corresponding LSOA. If there is no traffic counter within the LSOA, we back-off to outputting the ratio based on all traffic counters in the surrounding MSOA. If no traffic counters exist in the MSOA, we back-off to the value for the surrounding local-authority. Finally, for local-authorities with no traffic counts, a default value of zero is returned.

The implementation of the back-off field is as follows:

```JSON
{
  "fieldClass": "uk.org.tombolo.field.aggregation.BackOffField",
  "fields" : [
    {
      "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
      "recipe": "city-indices/active-transport/CyclingCount"
    },
    {
      "fieldClass": "uk.org.tombolo.field.aggregation.MapToContainingSubjectField",
      "subject": {
        "provider": "uk.gov.ons",
        "subjectType": "msoa"
      },
      "field": {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "recipe": "city-indices/active-transport/CyclingCount"
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
        "recipe": "city-indices/active-transport/CyclingCount"
      }
    },
    {
      "fieldClass": "uk.org.tombolo.field.value.FixedAnnotationField",
      "value" : "0.0"
    }
  ]
}
```

where cycling-count is defined using the built-in fields for aggregating traffic counts:

```JSON
{
  "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
  "operation": "div",
  "field1": {
    "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
    "recipe": "transport/traffic-counts-aggregated-bicycles"
  },
  "field2": {
    "fieldClass": "uk.org.tombolo.field.transformation.ListArithmeticField",
    "operation": "add",
    "fields": [
      {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "recipe": "transport/traffic-counts-aggregated-bicycles"
      },
      {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "recipe": "transport/traffic-counts-aggregated-cars"
      }
    ]
  }
}
```
see further the built-in recipes for aggregating [bicycle](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/modelling-fields/transport/traffic-counts-aggregated-bicycles-field.json) and [car](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/blob/master/src/main/resources/modelling-fields/transport/traffic-counts-aggregated-cars-field.json) traffic counts.


# Cycling infrastructure

The cycling infrastructure component of the active transport index is also a back-off field. In essence it uses Open Street Map data to calculate how large fraction of the entire road network is marked as having cycling infrastructure. The back-off field is implemented in similar way as above where we first try to calculate a value for the LSOA, but if none is available we first back off to the surrounding MSOA and eventually to the surrounding local authority. It is implemented as follows:

```JSON
{
  "fieldClass": "uk.org.tombolo.field.aggregation.BackOffField",
  "label": "Cycleway to Highway Count Ratio",
  "fields" : [
    {
      "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
      "label": "Cycleway to Highway Count lsoa",
      "recipe": "city-indices/active-transport/CycleLaneCount"
    },
    {
      "fieldClass": "uk.org.tombolo.field.aggregation.MapToContainingSubjectField",
      "label": "Cycleway to Highway Count msoa",
      "subject": {
        "provider": "uk.gov.ons",
        "subjectType": "msoa"
      },
      "field": {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "label": "Cycleway to Highway Count",
        "recipe": "city-indices/active-transport/CycleLaneCount"
      }
    },
    {
      "fieldClass": "uk.org.tombolo.field.aggregation.MapToContainingSubjectField",
      "label": "Cycleway to Highway Count localAuthority",
      "subject": {
        "provider": "uk.gov.ons",
        "subjectType": "localAuthority"
      },
      "field": {
        "fieldClass": "uk.org.tombolo.field.modelling.SingleValueModellingField",
        "label": "Cycleway to Highway Count",
        "recipe": "city-indices/active-transport/CycleLaneCount"
      }
    },
    {
      "fieldClass": "uk.org.tombolo.field.value.FixedAnnotationField",
      "label" : "default value",
      "value" : "0.0"
    }
  ]
}
```

where the CycleLaneCount model is an arithmetic-field where we divide the count of road segments with cycling infrastructure with the total count of road segments. In both cases the counts are calculated by using a attribute-matcher-field together inside a geographic-aggregation-field that aggregates over the corresponding geography (LSOA, MSOA or local-authority). It is implemented as follows.

```JSON
{
  "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
  "label": "Cycle to Road count Ratio",
  "operation": "div",
  "field1": {
    "fieldClass": "uk.org.tombolo.field.aggregation.GeographicAggregationField",
    "label": "Cycle Lane Count",
    "function": "sum",
    "subject" : {
      "provider": "org.openstreetmap",
      "subjectType": "OSMEntity"
    },
    "field": {
      "fieldClass": "uk.org.tombolo.field.assertion.AttributeMatcherField",
      "label": "Assert Cycle Lane",
      "attributes": [
        {
          "provider": "org.openstreetmap",
          "label": "cycleway"
        },
        {
          "provider": "org.openstreetmap",
          "label": "cycleway:left"
        },
        {
          "provider": "org.openstreetmap",
          "label": "cycleway:right"
        },
        {
          "provider": "org.openstreetmap",
          "label": "cycleway:oneway"
        },
        {
          "provider": "org.openstreetmap",
          "label": "cycleway:oneside"
        },
        {
          "provider": "org.openstreetmap",
          "label": "cycleway:otherside"
        }
      ],
      "field": {
        "fieldClass": "uk.org.tombolo.field.value.FixedAnnotationField",
        "value": "1"
      }
    }
  },
  "field2": {
    "fieldClass": "uk.org.tombolo.field.aggregation.GeographicAggregationField",
    "label": "Highway Count",
    "function": "sum",
    "subject" : {
      "provider": "org.openstreetmap",
      "subjectType": "OSMEntity"
    },
    "field": {
      "fieldClass": "uk.org.tombolo.field.assertion.AttributeMatcherField",
      "label": "Assert Highway",
      "attributes": [
        {
          "provider": "org.openstreetmap",
          "label": "highway"
        }
      ],
      "field": {
        "fieldClass": "uk.org.tombolo.field.value.FixedAnnotationField",
        "value": "1"
      }
    }
  }
}
```

# Active commute

The active-commute component uses data from the travel-to-work dataset from the UK census ([qs701ew](https://www.nomisweb.co.uk/census/2011/qs701ew)). It adds the number of people who either walk or cycle to work and divides by the total population size. The field code is as follows:

```JSON
{
  "fieldClass": "uk.org.tombolo.field.transformation.ArithmeticField",
  "label": "Active Transport to Work",
  "operation": "div",
  "field1": {
    "fieldClass": "uk.org.tombolo.field.transformation.ListArithmeticField",
    "label": "Sum Active modes",
    "operation": "add",
    "fields": [
      {
        "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
        "attribute": {
          "provider": "uk.gov.ons",
          "label": "Method of Travel to Work: On foot"
        }
      },
      {
        "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
        "attribute": {
          "provider": "uk.gov.ons",
          "label": "Method of Travel to Work: Bicycle"
        }
      }
    ]
  },
  "field2": {
    "fieldClass": "uk.org.tombolo.field.value.LatestValueField",
    "attribute": {
      "provider": "uk.gov.ons",
      "label": "Method of Travel to Work: All categories: Method of travel to work"
    }
  }
}
```
