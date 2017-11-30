### Open with QGIS

+ Install and open QGis
 
  Click [here](http://www.qgis.org/en/site/forusers/download.html) to download and choose the option relevant to your 
  operating 
  system.
 
+ Open the Digital Connector output in QGis by following these steps:
 
  + Click on **Layer -> Add Layer -> Add Vector Layer -> Browse (Choose the output file) -> Open**
  
    Now your screen should look something like this.
 
    ![Raw Output File](/readmeresources/qgis_images/raw_output.png)
 
  + Change the Projection to the British National Grid by clicking the button next to Render at the bottom of QGIS.
 
    ![Projection Options](/readmeresources/qgis_images/projection_option.png)
 
    In the Filter box type 27700 and choose British National Grid.
 
    ![Projection Search](/readmeresources/qgis_images/projection_search.png)
 
    Click OK, and now your screen should look something like this.
 
    ![Raw Output with UK Projection](/readmeresources/qgis_images/raw_output_uk_projection.png)
 
  + On left side under Layers Panel, Right Click on your filename layer which in this tutorial is “qgis_tutorial 
   OGRGeoJSON Polygon” and choose Properties. This page will allow you to style the map.
 
      ![Layers Panel](/readmeresources/qgis_images/layers_panel.png)
 
      Change the default settings as described:
      
      From top where it says “Single Symbol” choose “Categorized”. In Column choose “BicycleFraction”. In Color ramp 
      choose “Blues” and then click on “Classify” Button and on the pop up click “OK” and now your window should 
      look something like this.
 
      ![Layers Properties with options selected](/readmeresources/qgis_images/layer_properties_final_options.png)
 
      Click OK and Now your map should look something like this.
 
      ![Output without map in the background](/readmeresources/qgis_images/output_without_bg_map.png)
 
   + To add the background map layer click on Plugins -> Manage and Install Plugins
 
     ![Plugins menu](/readmeresources/qgis_images/plugin_option_menu.png)
 
     Plugin windows should appear. In case option “All” is not click and select it. Search for QuickMapServices and 
     then click on Install Plugin. Once the plugin is installed click Close.
 
     ![Plugins Search Window](/readmeresources/qgis_images/plugins_search.png)
 
     Now you should have “Globe” like icon on your toolbar. Click the Globe with + and Choose OSM -> OSM Standard.
 
     ![Quick Map Service Options](/readmeresources/qgis_images/osm_menu_option.png)
 
     Now your map should look like this.
 
     ![Final Output](/readmeresources/qgis_images/final_output.png)
 
   + Now to save as an image Go to Project -> Save as Image. 
    Give it a name of your choice and save it in your preferred directory. 
    The file you just saved should look like this.
 
     ![Output in PNG](/readmeresources/qgis_images/output_in_png.png)

