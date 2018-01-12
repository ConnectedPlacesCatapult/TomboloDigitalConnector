# Using the Digital Connector

In a nutshell, using the Digital Connector involves four steps:

 1. After ensuring that you have installed the pre-requisites outlined [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector#quick-start), install the Digital Connector by cloning the GitHub repository.
 2. Write a recipe file that describes the desired data output from the Digital Connector. This recipe includes which
  data-sources to use and how to mix the data together. To get started have a look at the [demo annotated recipe](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector/files/1543564/annotatedRecipe.pdf)
  or the other [examples](/src/main/resources/executions/examples/).
 3. [Run the export](README.md#run-export). In this step the Digital Connector will connect to the relevant data 
 sources, download the necessary data, join the data as per the user recipe and export the data in the requested output format. 
 4. Work with the data in tools such as QGIS, Jupyter Notebooks, R, etc.

## Example: Correlation between deprivation and childhood obesity 

Rosie wants to study the correlation between deprivation and childhood obesity.
She browses the importable data and finds that MSOA level childhood obesity is available from Public Health England and LSOA level deprivation score is available from DCLG. 
She writes in her recipe file that the output should be a CSV file with three columns.

- The first column is the MSOA identifier.
- The second column contains the percentage of obese children in that MSOA.
- The third column contains the median LSOA level deprivation value for the MSOA.

After exporting the data, Rosie loads it to a Jupyter Notebook where she uses R to calculate correlation coefficients and render a scatter-plot.

## Example: Cycling and pollution 

Thomas wants to visualise the relation between bicycle friendly boroughs in London and air quality.
He browses the importable data and finds traffic counts from Department for Transport and air quality measurements from the London Air Quality Network.
He writes a recipe saying that he wants the the shape of each borough as a feature.
He specifies that each borough should have three attributes:

- a name attribute;
- an attribute showing cycle traffic as a fraction of total traffic;
- and an attribute quantifying the air quality.

After exporting the data, Thomas opens the file in QGIS where he visualises it.

For a detailed use case see [Use Case on Cycling and Air Quality](Use-Case-on-Cycling-and-Air-Quality.md). 

## Example: Active Transport Index

As part of the Tombolo project we have built an application called [City Index Explorer](https://labs.tombolo.org.uk/city-index-explorer/). The application functions as a demonstrator of the possibilities for combining various urban data sources into an urban model. One of the indices we have developed for demonstration is the Active Transport Index. The index combines various data sources to assign a score to each LSOA representing the use and potential for active transport.

For details, see [Use Case on Active Transport Index](Use-Case-on-Active-Transport-Index.md)
