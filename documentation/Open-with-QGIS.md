### Open with QGIS

+ Install and open QGis
 
  Click [here](http://www.qgis.org/en/site/forusers/download.html) to download and choose the option relevant to your 
  operating 
  system.
 
+ Open the Digital Connector output in QGis by following these steps:
 
  + Click on **Layer -> Add Layer -> Add Vector Layer -> Browse (Choose the output file) -> Open**
  
    Now your screen should look something like this.
 
    ![Raw Output File](https://user-images.githubusercontent.com/14051876/33429690-ba36cb8c-d5c5-11e7-97d6-a3da8b917eb2.png)
 
  + Change the Projection to the British National Grid by clicking the button next to Render at the bottom of QGIS.
 
    ![Projection Options](https://user-images.githubusercontent.com/14051876/33429687-b9e95d8e-d5c5-11e7-893a-1d719fda620d.png)
 
    In the Filter box type 27700 and choose British National Grid.
 
    ![Projection Search](https://user-images.githubusercontent.com/14051876/33429688-ba026306-d5c5-11e7-9f88-4f93e00f8884.png)
 
    Click OK, and now your screen should look something like this.
 
    ![Raw Output with UK Projection](https://user-images.githubusercontent.com/14051876/33429689-ba1e5066-d5c5-11e7-8816-32ae9ba7ead5.png)
 
  + On left side under Layers Panel, Right Click on your filename layer which in this tutorial is “qgis_tutorial 
   OGRGeoJSON Polygon” and choose Properties. This page will allow you to style the map.
 
      ![Layers Panel](https://user-images.githubusercontent.com/14051876/33429679-b930f1c2-d5c5-11e7-96e0-9ca338ad69fa.png)
 
      Change the default settings as described:
      
      From top where it says “Single Symbol” choose “Categorized”. In Column choose “BicycleFraction”. In Color ramp 
      choose “Blues” and then click on “Classify” Button and on the pop up click “OK” and now your window should 
      look something like this.
 
      ![Layers Properties with options selected](https://user-images.githubusercontent.com/14051876/33429678-b9198b5e-d5c5-11e7-86fd-71f7c866d519.png)
 
      Click OK and Now your map should look something like this.
 
      ![Output without map in the background](https://user-images.githubusercontent.com/14051876/33429683-b985b7f2-d5c5-11e7-9855-83e4b8adc094.png)
 
   + To add the background map layer click on Plugins -> Manage and Install Plugins
 
     ![Plugins menu](https://user-images.githubusercontent.com/14051876/33429684-b99d00c4-d5c5-11e7-8f34-f39631017f9a.png)
 
     Plugin windows should appear. In case option “All” is not click and select it. Search for QuickMapServices and 
     then click on Install Plugin. Once the plugin is installed click Close.
 
     ![Plugins Search Window](https://user-images.githubusercontent.com/14051876/33429685-b9b9a22e-d5c5-11e7-926b-4cd370aa4d6f.png)
 
     Now you should have “Globe” like icon on your toolbar. Click the Globe with + and Choose OSM -> OSM Standard.
 
     ![Quick Map Service Options](https://user-images.githubusercontent.com/14051876/33429681-b9512d02-d5c5-11e7-8539-e311a38cb55d.png)
 
     Now your map should look like this.
 
     ![Final Output](https://user-images.githubusercontent.com/14051876/33429677-b8fc5796-d5c5-11e7-8dd3-159bbb657200.png)
 
   + Now to save as an image Go to Project -> Save as Image. 
    Give it a name of your choice and save it in your preferred directory. 
    The file you just saved should look like this.
 
     ![Output in PNG](https://user-images.githubusercontent.com/14051876/33429682-b96d18f0-d5c5-11e7-8ca5-b86f0eaa7376.png)

