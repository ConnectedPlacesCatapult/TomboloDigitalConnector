#Resources

This resources folder contains all the recipes currently available for use in the**Tombolo Digital Connector.** 
Within this folder, there are two sub-folders to be aware of:

**executions** 
- 
Contains examples of recipes in various formats which are further separated into two sub-folders. 

1. The**methodologicalexamples**folder provides a series of recipes that demonstrate the functionality of the Tombolo Digital Connector in different ways. If you are looking to use previous recipes as a way of building your own then using these is a good place to start.  
2. The**city-indices**folder contains recipes that generate*indexes*. In the context of the Tombolo Digital Connector an index is typically a combination of multiple components. For example, the summation of different measures from the census to generate a single number. Notice that in the Tombolo Digital Connector, these city-indices are often comprised of**modeling-fields**(discussed below) and the main index and its components are identified in the field `label`.

**modelling-fields** 
- 
Modelling fields are pre-specified fields, that can be 'plugged-in' to other recipes. If your recipe involves building a field that you think others may find valuable then it may be worth turning this field into a modelling-field for others to use in their recipes. This is often particularly valuable if your field is particularly nested and/or complex. 

The modelling fields are grouped into themes which relate to their purpose. All modelling-fields that are used by the city-indexes exist in the city-indices folder within modelling-fields. For more information on modelling-fields see [here](../executions/methodologicalexamples/modeling-fields/README.md)