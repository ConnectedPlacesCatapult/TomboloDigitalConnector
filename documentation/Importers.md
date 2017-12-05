In this section we describe the Tombolo Digital Connector importers. The role of importers is to connect to external or local data sources and import the appropriate data by reformatting it into the centralised data format. 

Importers are of three types:

- __Subject Importers__ import only Subjects. Examples include the LSOA and MSOA importers for the ONS Geographies Portal and the Health Organisation importer for NHS Choices data.
- __Timed Value Importers__ import only Timed Values for an externally defined set of Subjects. Examples include the ONS Census importer that import census values for geographies such as LSOAs, MSOAs and Local Authorities.
- __Mixed Importers__ import both Subjects and Timed Values. Examples include the Traffic Counts importer which imports both the locations of traffic counts as Subjects and the actual traffic counts as Timed Values.


We have built-in importers for a range of mostly public and open datasets. Users can use these importers directly to import the data they need in the processing. Additionally we have support for users to extend the Digital Connector with their own user defined custom importers for their local proprietary datasets. In the code base we have support tools such as Excel and Shapefile data extraction tools to make it easier to write custom importers.

## Built-in importers
Below you can find a list of built-in importers that were available at the time of writing. For an up to date list look at the [codebase](../tree/master/src/main/java/uk/org/tombolo/importer).

- Department for Communities and Local Government (DCLG)
  - Indices of multiple deprivation (value importer)
- Department for Education (DfE)
  - List of all schools (subject importer)
- Department for Transport (DfT)
  - Accessibility of output areas (value importer)
  - Traffic counts (mixed importer)
- London Air Quality Network (LAQN)
  - Air quality data from Environmental Research Group Kings College London (mixed importer)
- London Datastore
  - Borough profiles (value importer)
  - Public Health Outcomes Framework (PHOF) indicators for London (value importer)
  - Walking and cycling information of London boroughs (value importer)
- NHS Choices
  - Health Organisations (subject importer)
- Office of National Statistics (ONS)
  - Output area geometries (subject importer)
  - Census data (value importer)
  - Claimants data (value importer)
  - Wages data (value importer)
- Open Street Map (OSM)
  - Cycling infrastructure (subjects importer)
  - Greenspace data (subjects importer)
  - Land-use data (subjects importer)
  - Road infrastructure (subjects importer)
- Public Health England (PHE)
  - Adult Obesity data (value importer)
  - Childhood Obesity data (value importer)
- Space Syntax
  - Open Map (mixed importer)
- Transport for London (TfL)
  - Transport stations importer (mixed importer)
- Twitter
  - Geo-coded tweet importer (subject importer)
- Generic Importers
  - Generic CSV Importer (value importer / subject importer / mixed importer)

In the development of importers we have taken a pragmatic approach where we implement importers on need-to-use basis where the city challenges have been in the driver seat. As the project progresses we will be constantly supporting more data sources.
