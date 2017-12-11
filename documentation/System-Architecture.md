# System architecture

Figure 1 shows a high level system architecture for the Tombolo Digital Connector. The figure shows the main system components and the interlinking between them. We also show the links to external data and systems.

* At the core of the digital connector is the **Local Datastore** where all incoming data is stored in a centralised data format. 
* **Importers** are responsible for interpret the incoming data and code it in the centralised data format. We have both built-in importers for **publicly available datasources** as well as support for users to write their own importers for their **proprietary data**. If the proprietary data is available simple shape-files or simple csv files, it may be importable using a generic data importer without any additional implementation.
* **Exporters** are used to export data from the system. The output data can simply be the data values as they originated in the input data, or as a modelled combinations of various data-sources.
* For the modelling part we both have pre-defined **built-in model recipes** as well as support for the user to specify and export their own **proprietary model recipes**.

![High level system architecture](https://user-images.githubusercontent.com/14051876/33561208-f87e3b5e-d909-11e7-8309-a6a7edd0e941.png)

***Figure 1:*** _Overview of the Tombolo Digital Connector components and data workflow._