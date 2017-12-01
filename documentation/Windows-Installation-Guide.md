
# Windows Installation Guide

## Installing Java

### Check if java is already installed

- Click on the Start Button and type 'Command Prompt' and hit enter
- In the window type 'java -version'
- If your output looks like the one in the screenshot below, then probably java is not installed on your computer.
![Java not Installed](https://user-images.githubusercontent.com/19670372/33480435-a53ba3ce-d688-11e7-9ea3-ab96bb0a6ca8.png)

- It could be a possibility that java is installed but **Environment Variable** is not set.
- To check that go to 'C:\Program Files' and see if you see a folder named **Java**. If there is no folder like that 
then we can be sure that Java is not installed on your computer.
- Incase you find the Java folder there, then skip to **Set Environment Variable** Section

### Install Java

#### Check for OS Architecture

- Before we start downloading Java, we need to check the what is your OS Architecture in order to install
 the correct version of Java
- There are two ways to do it:
    - Through File Explorer
        - Click on Start -> File Explorer,  if that option is not available, open any folder
        - Right Click on 'This PC' and choose Properties as shown in the screenshot below
        ![Computer Properties](https://user-images.githubusercontent.com/19670372/33480444-a622a670-d688-11e7-85de-12be0f054004.png)
        - Once done look for the 'System Type' on a window similar to the one below
        ![OS Architecture](https://user-images.githubusercontent.com/19670372/33480441-a5d9bfe6-d688-11e7-8a19-251c3e0c4610.png)
        - If your system type is '64 bit operating system' then your system has 64 bit architecture
    - Through Command Prompt
        - Click on the start button and type ‘command prompt’, click on the application to open. 
        Once your command prompt is open type either of the following commands in order to get the architecture of the system
            - wmic os get osarchitecture
            ![OS Arc Command 1](https://user-images.githubusercontent.com/19670372/33480442-a5f1af66-d688-11e7-89b9-bc0b9e68fe98.png)
            - echo %PROCESSOR_ARCHITECTURE%
            ![OS Arc Command 2](https://user-images.githubusercontent.com/19670372/33480443-a60a6f56-d688-11e7-8e73-a653b45ee583.png)

#### Download and Install Java

- Once you know the architecture information of your system, go [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Accept the License and choose the version of **JDK** that suits the version of your operating system, e.g. if 
your system is 32 bit choose **'Windows x86'**, if it is 64 bit choose **'Windows x64'**
- In my case it happens to be 64 bit architecture, thus we are downloading ‘Windows x64’ version.
![Java Download](https://user-images.githubusercontent.com/19670372/33480429-a4b06124-d688-11e7-8785-a49425571722.png)
- Once downloaded, double click on the downloaded file, based on your security setting you might get a dialog to confirm opening of the .exe file, just click Run. Now you should see a dialog like this
![Java Download Setup](https://user-images.githubusercontent.com/19670372/33480430-a4c7f154-d688-11e7-8caf-0cbc7ec34a4d.png)
- Click Next. On the next screen make note of the installation directory, in my case it is installing in 
C:\Program Files\Java\jdk1.8.0_151\ which you can see at the bottom left side of the screen. This is required to set 
the environment variable at a later stage
![JDK installation folder](https://user-images.githubusercontent.com/19670372/33480431-a4defa84-d688-11e7-90e9-bc8b609c2209.png)
- Click Next. The installation will begin, while installing it may ask you to change the installation directory for 
**JRE**, just let it set to default and click Next.
![JRE Installation Folder](https://user-images.githubusercontent.com/19670372/33480432-a4f5c1f6-d688-11e7-86a0-81a79fbf93bb.png)
- Click Finish on the next screen, once the installation is completed.

##### Check Installation 

- Go to the command prompt and type 'java -version'
![Java not Installed](https://user-images.githubusercontent.com/19670372/33480435-a53ba3ce-d688-11e7-9ea3-ab96bb0a6ca8.png)
- If you see something like above then Environment Variable is not set.

##### Set Environment Variable

- Open File explorer -> Right click on 'This PC' -> Click on Properties, Once you do that you will get a window like 
this
![Advanced System Setting](https://user-images.githubusercontent.com/19670372/33480441-a5d9bfe6-d688-11e7-8a19-251c3e0c4610.png)
- Click on **Advanced System Settings**. You should get a Window like this
![System Properties](https://user-images.githubusercontent.com/19670372/33480436-a553f5aa-d688-11e7-9695-93b9d2c49f1e.png)
- Click on **Environment Variables**, which should give you a window like this
![Environment Variables](https://user-images.githubusercontent.com/19670372/33480398-a1f5fb06-d688-11e7-94d5-52084acfc562.png)
- Click on **Path** and Click **Edit**. In **Variable value** at the end type the path of the Java installation directory, 
for which you made the note earlier which in my case is ```C:\Program Files\Java\jdk1.8.0_151\``` and append **bin** at the 
end like this.
![Edit User Variable](https://user-images.githubusercontent.com/19670372/33480438-a58eb744-d688-11e7-9e32-1fb7ef77d86d.png)
- Click OK and open a new **Command Prompt** window (as explained earlier) and type **java -version**. 
Now you should see something like this.
![Java Installed](https://user-images.githubusercontent.com/19670372/33480434-a523df32-d688-11e7-953d-f948887cf6d5.png)

###### Congratulations you have successfully installed java, now let's move on to installing Gradle 


## Installing Gradle

### Check if gradle is installed

- Open **Command Prompt** and type **gradle --version**. If your output looks like this
![Gradle not Installed](https://user-images.githubusercontent.com/19670372/33480423-a424a0c6-d688-11e7-998f-5fc7bce27d74.png)
- Then the gradle application is not installed.

### Download and Install Gradle

- In order to install gradle go [here](https://gradle.org/releases/)  and scroll down for the latest version and click 
on complete as shown here
![Gradle Download](https://user-images.githubusercontent.com/19670372/33480421-a3f702ec-d688-11e7-8a1e-9063e2199fc2.png)
- Save the file and unzip it at your preferred location, in my case it is unzipped under **Program Files** in order to be 
consistent as application are usually installed under **Program Files**.
- Once unzipped navigate to the bin folder of gradle which in my case is 
```C:\Program Files\gradle-4.3.1-all\gradle-4.3.1\bin``` as the files were extracted in **Program Files** as described 
earlier. Copy the path of the **bin** folder by clicking the Address Bar in the File Explorer as shown in the screenshot below.
![Gradle folder path](https://user-images.githubusercontent.com/19670372/33480427-a4821364-d688-11e7-84d7-f7a5c772359d.png)
- Once the path is copied, follow the steps described in the Java Installation section to navigate to the 
**Environment Variable** windows, as shown below
![Env screen](https://user-images.githubusercontent.com/19670372/33480398-a1f5fb06-d688-11e7-94d5-52084acfc562.png)
- Choose **Path** and click **Edit**, you would see a window like this
![Gradle pre env screen](https://user-images.githubusercontent.com/19670372/33480424-a43adb8e-d688-11e7-8ab3-c75d8e518e8b.png)
- Click on **New** and Paste the path that you copied of Gradle bin, Now your window should look like this
![Gradle set env](https://user-images.githubusercontent.com/19670372/33480428-a498eea4-d688-11e7-96c8-02c0c3419afd.png)
- Click OK to save and dismiss this dialog box. Click OK again to dismiss the Environment Variable dialog box and 
Click OK to dismiss System Settings dialog. Now open a new window of **Command Prompt** and 
type **gradle --version** and you should see something like this
![Gradle installed](https://user-images.githubusercontent.com/19670372/33480422-a40da0a6-d688-11e7-8563-396df4d0775d.png)

###### Congratulations now you have gradle installed on your computer. Let’s move to install Postgresql

## Installing Postgresql and Postgis

### Install Postgresql

- To install **Postgresql**, go [here](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads#windows). 
You should see a screen something like
![Postgres download](https://user-images.githubusercontent.com/19670372/33480450-a6c0c742-d688-11e7-9897-0d9e48e93694.png)
- Choose the latest version and Select the operating system, based on the architecture of the system you need to 
choose the Operating system. In my case the system architecture is 64 bit, below screenshot displays the version 
and operating system selected
![Postgres os and version](https://user-images.githubusercontent.com/19670372/33480451-a6d7e968-d688-11e7-8af8-1194e79f6383.png)
- Once the options are selected, click Download Now. Once the file is downloaded double click to open it and 
click **Run**. The system will start doing some processing and after a second or two would give you **Set up** dialog.
Click **Next**
- It will ask you to choose the installation directory. Leave it to default and click **Next**, Click **Next** and 
Select Components screen but make sure all the options are by default checked, if they are not checked, 
check them and click **Next**
![Select Components](https://user-images.githubusercontent.com/19670372/33480477-a8f74f04-d688-11e7-9fb6-b382b7cfe3f1.png)
- On next screen, the setup process would ask to setup the data directory, leave it to default and click **Next**.
- On the next screen the installation process would prompt you to create **superuser** password, type in the password 
and make a note of it, it will be required at a later stage.
![Admin password Setup](https://user-images.githubusercontent.com/19670372/33480482-a95eaaa0-d688-11e7-99a8-a40ad6997124.png)

###### Note: The user account that postgresql creates is not called ```superuser``` it is called ```postgres``` but the account will have full access to the database.

- On the next screen, it will ask you to set the **port number**, leave it to default and click **Next**
![Port Number](https://user-images.githubusercontent.com/19670372/33480453-a709d34c-d688-11e7-86bb-136d774a6d30.png)
- On the next screen choose the **locale**, in my case it is **English, United Kingdom** as shown below. 
Once locale is chosen, click **Next**
![Pg Locale screen](https://user-images.githubusercontent.com/19670372/33480456-a73a0bca-d688-11e7-8b7b-327df9acad38.png)
- On the next screen, the process will show all the choices you have made in the installation process as shown below, 
Click **Next** and Click **Next** again
![Pg options screen](https://user-images.githubusercontent.com/19670372/33480458-a769b442-d688-11e7-9509-7d718e1336ac.png)
- The system will start installing postgresql, which could take a sometime. Once installed you should get a screen 
like below, make sure **Stack Builder** option is checked as we still need to download 
**PostGis** and other **JDBC** and **ODBC** drivers. Click **Finish**
![Pg installation complete](https://user-images.githubusercontent.com/19670372/33480455-a7222410-d688-11e7-8f31-fd0008cd25c1.png)

### Selecting Drivers and Spatial Extensions

- Stack builder installation dialog will pop up, choose the **Postgresql** version you just downloaded in the 
drop down box, Click **Next**
- On below screen, expand database drivers and choose, **pgJDBC** and **psqlODBC** as shown in the screenshot below. 
(make sure for **psqlODBC** you choose the  right architecture). Also expand the **Spatial Extensions** section 
and choose **PostGIS** option. Click **Next**
![Stack builder](https://user-images.githubusercontent.com/19670372/33480460-a796c66c-d688-11e7-8ded-bb6bca9ec4cc.png)
- Leave the download directory as default and click **Next** and click **Next** again

##### JDBC Installation
- Click **Next** on the below screen and **JDBC** installation will begin
![Jdbc installation](https://user-images.githubusercontent.com/19670372/33480465-a7dce890-d688-11e7-8a21-15ca035b3ac0.png)

- Once the installation is done you would see a screen like this, but the installation is only completed for 
one driver, we still have got 2 more drivers to install as we have selected 3 drivers on Stack Builder. 
Click **Finish**

##### ODBC Installation

- Once you are done with the **JDBC** setup, next setup will start which is **ODBC**, click **Next**
- Leave the default installation directory and click **Next** and Click **Next** again and installation will begin
![ODBC Installation](https://user-images.githubusercontent.com/19670372/33480468-a820a3d2-d688-11e7-8d84-1622527f29e6.png)
- Once the installation is done, click **Finish**.

##### Postgis Installation

-  Now the third installation will start which is **PostGis**, accept the terms and conditions by clicking **I Agree**
![Postgis agreement](https://user-images.githubusercontent.com/19670372/33480470-a8534a30-d688-11e7-9690-6c5fa4467017.png)
- Click **Next** on **Choose Components** screen and make sure **PostGIS** is selected.
- Leave the installation directory to default on **Choose Install Location** screen and Click **Next**
- Click **Yes** on all the below shown dialogs
![postgis gdal data](https://user-images.githubusercontent.com/19670372/33480473-a899534a-d688-11e7-8b73-276a3a2aba35.png)
![postgis rasters](https://user-images.githubusercontent.com/19670372/33480474-a8af6680-d688-11e7-903b-db1f7ac394a4.png)
![postgis rasters 2](https://user-images.githubusercontent.com/19670372/33480475-a8c4d6b4-d688-11e7-902d-081b4112981a.png)
- Click **Close** when the installation is finished
- Click **Finish** on **Stack Builder Installation Completed** screen

###### Note: We still have to set the Environment Variable path, as psql command will still not work on Command Prompt

##### Set Environment Variable

- Navigate to the **bin** directory of postgresql installation, which in my case is 
```C:\Program Files\PostgreSQL\10\bin```. Click on the address bar of the file explorer window and copy the path.
- Navigate to the **Environment variable** window as described in the Java installation section, and when you are on 
**Edit environment variable** screen, click on **New** and paste the address of the **Postgresql bin directory** that 
you copied a little while ago and click **OK**
![Postgres set env](https://user-images.githubusercontent.com/19670372/33480480-a9295102-d688-11e7-8345-be665b7ab077.png)
- Now open a new window of **Command Prompt** and type ```psql -U postgres``` as by default **Postgresql** creates a 
**postgres** account, upon entering the above command it will prompt you to enter password for **postgres** user which 
you have set while installing the Postgresql. Type the same password in and you should see something like below. 
Screenshot below shows a Postgresql shell. Type ```\q``` to quit.
![Postgres installed](https://user-images.githubusercontent.com/19670372/33480397-a1de3f48-d688-11e7-9545-2475db426231.png)

###### Congratulations now you have installed Postgres and Postgis.

## Installing Git

### Check if Git is already installed

- Lets first check if Git is installed on your machine or not. Open a new **Command prompt** 
window and type ```git --version```, if you see something like below then we could say that git is not 
installed on your computer. It could be that Git is installed but **Environment Variable** is not set. 
You could check that by going to the ```C:\Program Files``` if you would see a folder named **Git** then you just need 
to set **Environment Variable** path. If that is the case please feel free to the section 
where we are setting **Environment Variable**
![Git not installed](https://user-images.githubusercontent.com/19670372/33480415-a36af86a-d688-11e7-8180-9b8031e1f6e5.png)

### Download Git

- Go [here](https://github.com/git-for-windows/git/releases/tag/v2.15.0.windows.1) and scroll down to **Downloads** 
section and click the latest version of the application which has **.exe** extension, which in my case is 
**Git-2.15.0-32-bit.exe**
![Git Download](https://user-images.githubusercontent.com/19670372/33480401-a250b5aa-d688-11e7-90fc-29d206406e01.png)

### Install Git

- Double click on the file once it is downloaded and click **Run**.
- Click **Next** on the license page
- Leave the installation directory to default on **Select Destination Location** box and click **Next**
- Click **Next** on **Select Components** box
- Click **Next** on **Select Start Menu Folder** box
- Click **Next** on **Adjust your PATH environment** box 
- Click **Next** on **Choosing HTTPS transport backend** box
- Click **Next** on **Configuring the line ending conversions** box
- Click **Next** on **Configuring the terminal emulator to use with Git Bash** box
- Click **Install** on **Configuring extra options**, this will start installing Git on your machine
- Once the installation is complete, click **Finish**

###### Note: We still need to set environment variable in order for git command to work from Command Prompt

### Set Environment Variable

- Navigate to the **bin** directory of **Git** which in my case is ```C:\Program Files\Git\bin```. 
Click on the address bar of the file explorer and copy the path.
![Set git env](https://user-images.githubusercontent.com/19670372/33480416-a381480e-d688-11e7-9c60-cf644fd83545.png)
- Navigate to the **Environment variable** window as described in the Java installation section, and when you are on 
**Edit environment variable** screen, click on **New** and paste the address of the **Git bin directory path** that 
you copied a little while ago and click **OK**
![Git env set](https://user-images.githubusercontent.com/19670372/33480418-a3b0c3fe-d688-11e7-907f-012576d757a1.png)
- To test the installation again, open a new **Command Prompt** and type ```git --version``` 
and you should see something like this.
![Git installed](https://user-images.githubusercontent.com/19670372/33480414-a354ef7a-d688-11e7-887d-de3660c2dfe5.png)

###### Congratulations, you now have git installed on your machine.

##### Now that we have all the dependencies installed, let’s start working with Tombolo

## Clone Tombolo Digital Connector Repository

- To clone Tombolo Digital Connector repository, click [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector) 
then click on **Clone or Download** and copy the path
![Git tombolo clone](https://user-images.githubusercontent.com/19670372/33480399-a20dbd86-d688-11e7-82bf-c035770ee56e.png)
- Open a new **Command Prompt** and make sure that you present working directory is your home directory. 
You can check that by looking the path in the **Command Prompt** window before **>**, in my case is looks like this
![Check present working dir](https://user-images.githubusercontent.com/19670372/33480395-a1afea4e-d688-11e7-961e-c93e1d2f9c24.png)
- If yours also follows the same pattern as ```C:\Users\<user-name>``` then you are good to go. If not then change 
it by typing ```cd C:\Users\<user-name>```. Now that we are in our home directory, we can type following 
commands to clone **TomboloDigitalConnector** repo.
```
cd Desktop
mkdir Tombolo
cd Tombolo
git clone https://github.com/FutureCitiesCatapult/TomboloDigitalConnector.git

```
- Now wait for it to download, when it is downloaded you would see something like below.
![Git cloning tombolo](https://user-images.githubusercontent.com/19670372/33480400-a2396af8-d688-11e7-9309-5967c32faa63.png)
- Now type ```cd TomboloDigitalConnector```.
- We have rename 2 project files **gradle.properties.example** and **apikeys.properties.example**. 
To do that just type the following commands in **Command Prompt**

```
rename gradle.properties.example gradle.properties
rename apikeys.properties.example apikeys.properties

``` 

### Setup Databases

#### Main Database

- Type the following commands

```

psql -U postgres
Enter the password for the account and hit enter (You have set this password while installing postgres)
CREATE USER tombolo WITH PASSWORD ‘tombolo’;
CREATE DATABASE tombolo WITH ENCODING=UTF8;

``` 
![Pg create role](https://user-images.githubusercontent.com/19670372/33480447-a66bd0f2-d688-11e7-93ee-db8889d0e5a7.pngg)
![Pg create database](https://user-images.githubusercontent.com/19670372/33480393-a19258ee-d688-11e7-8f8d-7bb9c38f29a5.png)

- Make **tombolo** a **superuser**

```
ALTER USER tombolo WITH SUPERUSER;
```
![Pg alter role](https://user-images.githubusercontent.com/19670372/33480445-a639d7be-d688-11e7-8e14-7af3fd80e220.png)

- Type ```\q``` to quit
- We now need to create **postgis** extension

```
psql -d tombolo -U tombolo
Enter the password as tombolo and now Type the following commands
CREATE EXTENSION postgis;
SET NAMES ‘UTF8’;

```
![Pg create extension](https://user-images.githubusercontent.com/19670372/33480446-a6517176-d688-11e7-94dc-0ba0dad55fa2.png)

- Type ```\q``` to quit
- Now run the sql script to setup your database

```
psql -d tombolo -U tombolo < src\main\resources\sql\create_database.sql
Enter the password as ‘tombolo’

```
![Tombolo script output](https://user-images.githubusercontent.com/19670372/33480481-a945a49c-d688-11e7-84ab-e4a55e6201cc.png)

#### Test Database

- The steps for setting up the test database is same as setting up the main database. Thus follow the steps 
starting from **Main Database** section and replace **tombolo** with **tombolo_test** everywhere even 
for the passwords. Below are just the screenshots for the steps:
![Tombolo Test database](https://user-images.githubusercontent.com/19670372/33480448-a6867920-d688-11e7-97ab-1086856d518b.png)
![Tombolo Test database 2](https://user-images.githubusercontent.com/19670372/33480449-a69c291e-d688-11e7-86b4-d894a44df050.png)

###### Congratulations now your both the databases are setup. 

### Build and Run the project

#### Build

- Now you are all set to build the project **TomboloDigitalConnector**. To build the project 
in **Command Prompt** type the following

```
gradle clean build
```
- Running the aforementioned command should start downloading the packages required to run **DigitalConnector**, 
once the packages is downloaded it will start building the **DigitalConnector** and start running the tests, on a 
machine with 8gb of ram it should not take more than 15 minutes to run the tests, once the downloads are done.
- Below are couple screenshots of the start of the process and end of the process.
![Gradle build](https://user-images.githubusercontent.com/19670372/33480419-a3c9541e-d688-11e7-8adc-b88e537e04ea.png)
![Gradle build 2](https://user-images.githubusercontent.com/19670372/33480420-a3e0723e-d688-11e7-9bc0-5e72d968fe9a.png)

###### Congratulations you have successfully built the Digital Connector. Now let’s try to run an example.

#### Run

- Type the following command in the **Command Prompt**

```
gradle runExport ^
-PdataExportSpecFile=src\main\resources\executions\examples\reaggregate-traffic-count-to-la.json ^ 
-PoutputFile=reaggregate-traffic-count-to-la_output.json
```

- Below are couple of screenshots of start and end of the process.
![Gradle run export 1](https://user-images.githubusercontent.com/19670372/33480425-a45082ae-d688-11e7-8943-921fba383aba.png)
![Gradle run export 2](https://user-images.githubusercontent.com/19670372/33480426-a46b23d4-d688-11e7-905c-227e5c9a7e45.png)

##### View output in a file

```
start notepad reaggregate-traffic-count-to-la_output.json
```

##### View output in a Folder

```
start .
```

  


 


