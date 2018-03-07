
# Windows Installation Guide

## Table of Contents

- [Installing Java](#installing-java)
- [Installing Gradle](#installing-gradle)
- [Installing Postgresql and Postgis](#installing-postgresql-and-postgis)
- [Installing Git](#installing-git)
- [Clone Tombolo Digital Connector Repository](#clone-tombolo-digital-connector-repository)

## Installing Java

### Check if java is already installed

- Click on the Start Button and type 'Command Prompt' and hit enter
- In the window type 'java -version'
- If your output looks like the one in the screenshot below, then probably java is not installed on your computer.
![Java not Installed](https://user-images.githubusercontent.com/19670372/33494868-7de2792c-d6bc-11e7-81b1-5b9726168cf4.png)

- It could be a possibility that java is installed but **Environment Variable** is not set.
- To check that go to 'C:\Program Files' and see if you see a folder named **Java**. If there is no folder like that 
then we can be sure that Java is not installed on your computer.
- In case you find the Java folder there, then skip to **Set Environment Variable** Section

### Install Java

#### Check for OS Architecture

- Before we start downloading Java, we need to check the what is your OS Architecture in order to install
 the correct version of Java
- There are two ways to do it:
    - Through File Explorer
        - Click on Start -> File Explorer,  if that option is not available, open any folder
        - Right Click on 'This PC' and choose Properties as shown in the screenshot below
        ![Computer Properties](https://user-images.githubusercontent.com/19670372/33494857-7d06ce40-d6bc-11e7-99cf-b46a15f37e68.png)
        - Once done look for the 'System Type' on a window similar to the one below
        ![OS Architecture](https://user-images.githubusercontent.com/19670372/33494861-7d4fbad8-d6bc-11e7-8d7e-28b299621a11.png)
        - If your system type is '64 bit operating system' then your system has 64 bit architecture
    - Through Command Prompt
        - Click on the start button and type ‘command prompt’, click on the application to open. 
        Once your command prompt is open type either of the following commands in order to get the architecture of the system
            - wmic os get osarchitecture  
            ![OS Arc Command 1](https://user-images.githubusercontent.com/19670372/33494860-7d37bf3c-d6bc-11e7-85e5-10f1f35addda.png)
            - echo %PROCESSOR_ARCHITECTURE%  
            ![OS Arc Command 2](https://user-images.githubusercontent.com/19670372/33494858-7d1f2788-d6bc-11e7-8e12-e6c06d10163a.png)

#### Download and Install Java

- Once you know the architecture information of your system, go [here](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- Accept the License and choose the version of **JDK** that suits the version of your operating system, e.g. if 
your system is 32 bit choose **'Windows x86'**, if it is 64 bit choose **'Windows x64'**
- In my case it happens to be 64 bit architecture, thus we are downloading ‘Windows x64’ version.
![Java Download](https://user-images.githubusercontent.com/19670372/33494875-7e7da83e-d6bc-11e7-8038-3c32e770e783.png)
- Once downloaded, double click on the downloaded file, based on your security setting you might get a dialog to confirm opening of the .exe file, just click Run. Now you should see a dialog like this
![Java Download Setup](https://user-images.githubusercontent.com/19670372/33494873-7e618f00-d6bc-11e7-96bc-1ac14f8b82ab.png)
- Click Next. On the next screen make note of the installation directory, in my case it is installing in 
C:\Program Files\Java\jdk1.8.0_151\ which you can see at the bottom left side of the screen. This is required to set 
the environment variable at a later stage  
![JDK installation folder](https://user-images.githubusercontent.com/19670372/33494872-7e45c518-d6bc-11e7-89ee-1429577a5d61.png)
- Click Next. The installation will begin, while installing it may ask you to change the installation directory for 
**JRE**, just let it set to default and click Next.  
![JRE Installation Folder](https://user-images.githubusercontent.com/19670372/33494871-7e2cadf8-d6bc-11e7-84fc-10864c159c69.png)
- Click Finish on the next screen, once the installation is completed.

##### Check Installation 

- Go to the command prompt and type 'java -version'
![Java not Installed](https://user-images.githubusercontent.com/19670372/33494868-7de2792c-d6bc-11e7-81b1-5b9726168cf4.png)
- If you see something like above then Environment Variable is not set.

##### Set Environment Variable

- Open File explorer -> Right click on 'This PC' -> Click on Properties, Once you do that you will get a window like 
this  
![Advanced System Setting](https://user-images.githubusercontent.com/19670372/33494861-7d4fbad8-d6bc-11e7-8d7e-28b299621a11.png)
- Click on **Advanced System Settings**. You should get a Window like this
![System Properties](https://user-images.githubusercontent.com/19670372/33494867-7dc95e9c-d6bc-11e7-8462-767075c59c74.png)
- Click on **Environment Variables**, which should give you a window like this
![Environment Variables](https://user-images.githubusercontent.com/19670372/33494880-7ef94e94-d6bc-11e7-8743-a3aee641e77d.png)
- Click on **Path** and Click **Edit**. In **Variable value** at the end type the path of the Java installation directory, 
for which you made the note earlier which in my case is ```C:\Program Files\Java\jdk1.8.0_151\``` and append **bin** at the 
end like this.  
![Edit User Variable](https://user-images.githubusercontent.com/19670372/33494865-7d9ac64a-d6bc-11e7-8e5d-534d1e7bd691.png)
- Click OK and open a new **Command Prompt** window (as explained earlier) and type **java -version**. 
Now you should see something like this.  
![Java Installed](https://user-images.githubusercontent.com/19670372/33494869-7dfd288a-d6bc-11e7-8aaa-5ae94faf4ca8.png)

###### Congratulations you have successfully installed java, now let's move on to installing Gradle 

## Installing Gradle

### Check if gradle is installed

- Open **Command Prompt** and type **gradle --version**. If your output looks like this
![Gradle not Installed](https://user-images.githubusercontent.com/19670372/33494881-7f11dff4-d6bc-11e7-838e-78788c9f6bef.png)
- Then the gradle application is not installed.

### Download and Install Gradle

- In order to install gradle go [here](https://gradle.org/releases/)  and scroll down for the latest version and click 
on complete as shown here
![Gradle Download](https://user-images.githubusercontent.com/19670372/33494883-7f4a4d4e-d6bc-11e7-81ed-a87841741f9c.png)
- Save the file and unzip it at your preferred location, in my case it is unzipped under **Program Files** in order to be 
consistent as application are usually installed under **Program Files**.
- Once unzipped navigate to the bin folder of gradle which in my case is 
```C:\Program Files\gradle-4.3.1-all\gradle-4.3.1\bin``` as the files were extracted in **Program Files** as described 
earlier. Copy the path of the **bin** folder by clicking the Address Bar in the File Explorer as shown in the screenshot below.
![Gradle folder path](https://user-images.githubusercontent.com/19670372/33494877-7eacedce-d6bc-11e7-83d4-d2e907f2c226.png)
- Once the path is copied, follow the steps described in the Java Installation section to navigate to the 
**Environment Variable** windows, as shown below
![Env screen](https://user-images.githubusercontent.com/19670372/33494880-7ef94e94-d6bc-11e7-8743-a3aee641e77d.png)
- Choose **Path** and click **Edit**, you would see a window like this
![Gradle pre env screen](https://user-images.githubusercontent.com/19670372/33494880-7ef94e94-d6bc-11e7-8743-a3aee641e77d.png)
- Click on **New** and Paste the path that you copied of Gradle bin, Now your window should look like this
![Gradle set env](https://user-images.githubusercontent.com/19670372/33494876-7e955588-d6bc-11e7-9987-da52310bccac.png)
- Click OK to save and dismiss this dialog box. Click OK again to dismiss the Environment Variable dialog box and 
Click OK to dismiss System Settings dialog. Now open a new window of **Command Prompt** and 
type **gradle --version** and you should see something like this
![Gradle installed](https://user-images.githubusercontent.com/19670372/33494882-7f2a85ae-d6bc-11e7-8812-69bc386fcbd0.png)

###### Congratulations now you have gradle installed on your computer. Let’s move to install Postgresql

## Installing Postgresql and Postgis

### Install Postgresql

- To install **Postgresql**, go [here](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads#windows). 
You should see a screen something like  
![Postgres download](https://user-images.githubusercontent.com/19670372/33494850-7c51475a-d6bc-11e7-9ea4-dd678e37a1b5.png)
- Choose the latest version and Select the operating system, based on the architecture of the system you need to 
choose the Operating system. In my case the system architecture is 64 bit, below screenshot displays the version 
and operating system selected  
![Postgres os and version](https://user-images.githubusercontent.com/19670372/33494849-7c3993c6-d6bc-11e7-936d-2a5f9bc23969.png)
- Once the options are selected, click Download Now. Once the file is downloaded double click to open it and 
click **Run**. The system will start doing some processing and after a second or two would give you **Set up** dialog.
Click **Next**
- It will ask you to choose the installation directory. Leave it to default and click **Next**, Click **Next** and 
Select Components screen but make sure all the options are by default checked, if they are not checked, 
check them and click **Next**  
![Select Components](https://user-images.githubusercontent.com/19670372/33494824-79cd5e4c-d6bc-11e7-951f-ed0301bc6e40.png)
- On next screen, the setup process would ask to setup the data directory, leave it to default and click **Next**.
- On the next screen the installation process would prompt you to create **superuser** password, type in the password 
and make a note of it, it will be required at a later stage.  
![Admin password Setup](https://user-images.githubusercontent.com/19670372/33494820-797176ea-d6bc-11e7-9658-aca504bef294.png)

###### Note: The user account that postgresql creates is not called ```superuser``` it is called ```postgres``` but the account will have full access to the database.

- On the next screen, it will ask you to set the **port number**, leave it to default and click **Next**  
![Port Number](https://user-images.githubusercontent.com/19670372/33494847-7c072fbc-d6bc-11e7-9728-72dc0d89ddd2.png)
- On the next screen choose the **locale**, in my case it is **English, United Kingdom** as shown below. 
Once locale is chosen, click **Next**  
![Pg Locale screen](https://user-images.githubusercontent.com/19670372/33494845-7bced54a-d6bc-11e7-92d5-fdac11aa251d.png)
- On the next screen, the process will show all the choices you have made in the installation process as shown below, 
Click **Next** and Click **Next** again  
![Pg options screen](https://user-images.githubusercontent.com/19670372/33494843-7b9d4d86-d6bc-11e7-83c6-b101a798be50.png)
- The system will start installing postgresql, which could take a sometime. Once installed you should get a screen 
like below, make sure **Stack Builder** option is checked as we still need to download 
**PostGis** and other **JDBC** and **ODBC** drivers. Click **Finish**  
![Pg installation complete](https://user-images.githubusercontent.com/19670372/33494846-7be6c9f2-d6bc-11e7-874c-2f10d1a84e07.png)

### Selecting Drivers and Spatial Extensions

- Stack builder installation dialog will pop up, choose the **Postgresql** version you just downloaded in the 
drop down box, Click **Next**
- On below screen, expand database drivers and choose, **pgJDBC** and **psqlODBC** as shown in the screenshot below. 
(make sure for **psqlODBC** you choose the  right architecture). Also expand the **Spatial Extensions** section 
and choose **PostGIS** option. Click **Next**
![Stack builder](https://user-images.githubusercontent.com/19670372/33494841-7b6dcf5c-d6bc-11e7-8b8b-6074c48097b3.png)
- Leave the download directory as default and click **Next** and click **Next** again

##### JDBC Installation
- Click **Next** on the below screen and **JDBC** installation will begin
![Jdbc installation](https://user-images.githubusercontent.com/19670372/33494838-7b0e7480-d6bc-11e7-97f2-2d55d7bf656a.png)

- Once the installation is done you would see a screen like this, but the installation is only completed for 
one driver, we still have got 2 more drivers to install as we have selected 3 drivers on Stack Builder. 
Click **Finish**

##### ODBC Installation

- Once you are done with the **JDBC** setup, next setup will start which is **ODBC**, click **Next**
- Leave the default installation directory and click **Next** and Click **Next** again and installation will begin
![ODBC Installation](https://user-images.githubusercontent.com/19670372/33494835-7ac5598a-d6bc-11e7-93f4-29e01690b677.png)
- Once the installation is done, click **Finish**.

##### Postgis Installation

-  Now the third installation will start which is **PostGis**, accept the terms and conditions by clicking **I Agree**  
![Postgis agreement](https://user-images.githubusercontent.com/19670372/33494832-7a93ce24-d6bc-11e7-993b-d8e99c2bdd23.png)
- Click **Next** on **Choose Components** screen and make sure **PostGIS** is selected.
- Leave the installation directory to default on **Choose Install Location** screen and Click **Next**
- Click **Yes** on all the below shown dialogs  
![postgis gdal data](https://user-images.githubusercontent.com/19670372/33494829-7a455e38-d6bc-11e7-8c98-51c8a91c03d6.png)
![postgis rasters](https://user-images.githubusercontent.com/19670372/33494827-7a2c323c-d6bc-11e7-8e67-0a9f5ed5d5ef.png)
![postgis rasters 2](https://user-images.githubusercontent.com/19670372/33494826-79fad25a-d6bc-11e7-8b63-1a35044e23d4.png)
- Click **Close** when the installation is finished
- Click **Finish** on **Stack Builder Installation Completed** screen

###### Note: We still have to set the Environment Variable path, as psql command will still not work on Command Prompt

##### Set Environment Variable

- Navigate to the **bin** directory of postgresql installation, which in my case is 
```C:\Program Files\PostgreSQL\10\bin```. Click on the address bar of the file explorer window and copy the path.
- Navigate to the **Environment variable** window as described in the Java installation section, and when you are on 
**Edit environment variable** screen, click on **New** and paste the address of the **Postgresql bin directory** that 
you copied a little while ago and click **OK**  
![Postgres set env](https://user-images.githubusercontent.com/19670372/33494822-799f2e1e-d6bc-11e7-9ddc-784b053dfb31.png)
- Now open a new window of **Command Prompt** and type ```psql -U postgres``` as by default **Postgresql** creates a 
**postgres** account, upon entering the above command it will prompt you to enter password for **postgres** user which 
you have set while installing the Postgresql. Type the same password in and you should see something like below. 
Screenshot below shows a Postgresql shell. Type ```\q``` to quit.  
![Postgres installed](https://user-images.githubusercontent.com/19670372/33494910-81b6fa3c-d6bc-11e7-9302-e57a9d19a9bc.png)

###### Congratulations now you have installed Postgres and Postgis.

## Installing Git

### Check if Git is already installed

- Lets first check if Git is installed on your machine or not. Open a new **Command prompt** 
window and type ```git --version```, if you see something like below then we could say that git is not 
installed on your computer. It could be that Git is installed but **Environment Variable** is not set. 
You could check that by going to the ```C:\Program Files``` if you would see a folder named **Git** then you just need 
to set **Environment Variable** path. If that is the case please feel free to the section 
where we are setting **Environment Variable**  
![Git not installed](https://user-images.githubusercontent.com/19670372/33494892-8011399a-d6bc-11e7-82a9-7efc9b2bf130.png)

### Download Git

- Go [here](https://github.com/git-for-windows/git/releases/tag/v2.15.0.windows.1) and scroll down to **Downloads** 
section and click the latest version of the application which has **.exe** extension, which in my case is 
**Git-2.15.0-32-bit.exe**  
![Git Download](https://user-images.githubusercontent.com/19670372/33494905-814dfa50-d6bc-11e7-9961-c469df209243.png)

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
![Set git env](https://user-images.githubusercontent.com/19670372/33494890-7ff4051e-d6bc-11e7-8bee-84b1ede0414c.png)
- Navigate to the **Environment variable** window as described in the Java installation section, and when you are on 
**Edit environment variable** screen, click on **New** and paste the address of the **Git bin directory path** that 
you copied a little while ago and click **OK**  
![Git env set](https://user-images.githubusercontent.com/19670372/33494887-7fb26898-d6bc-11e7-9abb-9565330b9ba1.png)
- To test the installation again, open a new **Command Prompt** and type ```git --version``` 
and you should see something like this.  
![Git installed](https://user-images.githubusercontent.com/19670372/33494893-80291830-d6bc-11e7-9bff-9461fe5f4689.png)

###### Congratulations, you now have git installed on your machine.

##### Now that we have all the dependencies installed, let’s start working with Tombolo

## Clone Tombolo Digital Connector Repository

- To clone Tombolo Digital Connector repository, click [here](https://github.com/FutureCitiesCatapult/TomboloDigitalConnector) 
then click on **Clone or Download** and copy the path
![Git tombolo clone](https://user-images.githubusercontent.com/19670372/33494907-8183c6b2-d6bc-11e7-8ad5-e743b46513ef.png)
- Open a new **Command Prompt** and make sure that you present working directory is your home directory. 
You can check that by looking the path in the **Command Prompt** window before **>**, in my case is looks like this
![Check present working dir](https://user-images.githubusercontent.com/19670372/33494912-81f2adde-d6bc-11e7-9de5-0f40a097bba9.png)
- If yours also follows the same pattern as ```C:\Users\<user-name>``` then you are good to go. If not then change 
it by typing ```cd C:\Users\<user-name>```. Now that we are in our home directory, we can type following 
commands to clone **TomboloDigitalConnector** repo.

```bash
cd Desktop
mkdir Tombolo
cd Tombolo
git clone https://github.com/FutureCitiesCatapult/TomboloDigitalConnector.git
```

- Now wait for it to download, when it is downloaded you would see something like below.
![Git cloning tombolo](https://user-images.githubusercontent.com/19670372/33494906-8167d1e6-d6bc-11e7-951c-8d898167e836.png)
- Now type ```cd TomboloDigitalConnector```.
- We have rename 2 project files **gradle.properties.example** and **apikeys.properties.example**. 
To do that just type the following commands in **Command Prompt**

```bash
rename gradle.properties.example gradle.properties
rename apikeys.properties.example apikeys.properties
``` 

### Setup Databases

#### Main Database

- Type the following commands

```bash
psql -U postgres
Enter the password for the account and hit enter (You have set this password while installing postgres)
CREATE USER tombolo WITH PASSWORD ‘tombolo’;
CREATE DATABASE tombolo WITH ENCODING=UTF8;
``` 
![Pg create role](https://user-images.githubusercontent.com/19670372/33494853-7c9cefd4-d6bc-11e7-8838-41598ffabbfa.png)
![Pg create database](https://user-images.githubusercontent.com/19670372/33494855-7cd1b200-d6bc-11e7-83f1-4dd434831d3e.png)

- Make **tombolo** a **superuser**

```bash
ALTER USER tombolo WITH SUPERUSER;
```
![Pg alter role](https://user-images.githubusercontent.com/19670372/33494856-7cee79f8-d6bc-11e7-8767-df5edc9f069d.png)

- Type ```\q``` to quit
- We now need to create **postgis** extension

```bash
psql -d tombolo -U tombolo
Enter the password as tombolo and now Type the following commands
CREATE EXTENSION postgis;
SET NAMES ‘UTF8’;
```
![Pg create extension](https://user-images.githubusercontent.com/19670372/33494854-7cb9e724-d6bc-11e7-8391-0e02f9bdf069.png)

- Type ```\q``` to quit
- Now run the sql script to setup your database

```bash
psql -d tombolo -U tombolo < src\main\resources\sql\create_database.sql
Enter the password as ‘tombolo’
```
![Tombolo script output](https://user-images.githubusercontent.com/19670372/33494821-79893352-d6bc-11e7-8447-7f86b0a3fd61.png)

#### Test Database
 ###### IMPORTANT NOTE: This database is not optional, if not set up the tests will fail.

- The steps for setting up the test database is same as setting up the main database. Thus follow the steps 
starting from **Main Database** section and replace **tombolo** with **tombolo_test** everywhere even 
for the passwords. Below are just the screenshots for the steps:
![Tombolo Test database](https://user-images.githubusercontent.com/19670372/33494852-7c82926a-d6bc-11e7-8ec8-c6299e365501.png)
![Tombolo Test database 2](https://user-images.githubusercontent.com/19670372/33494851-7c6ad0f8-d6bc-11e7-9074-9b71f0482550.png)

###### Congratulations now your both the databases are setup. 

### Build and Run the project

#### Build

- Now you are all set to build the project **TomboloDigitalConnector**. To build the project 
in **Command Prompt** type the following

```bash
gradle clean build
```
- Running the aforementioned command should start downloading the packages required to run **DigitalConnector**, 
once the packages is downloaded it will start building the **DigitalConnector** and start running the tests, on a 
machine with 8gb of ram it should not take more than 15 minutes to run the tests, once the downloads are done.
- Below are couple screenshots of the start of the process and end of the process.
![Gradle build](https://user-images.githubusercontent.com/19670372/33494886-7f9a2e72-d6bc-11e7-880f-1e2cd2f1d2cb.png)
![Gradle build 2](https://user-images.githubusercontent.com/19670372/33494885-7f641d8c-d6bc-11e7-8628-e52ddd280ed7.png)

###### Congratulations you have successfully built the Digital Connector. Now let’s try to run an example.

#### Run

- Type the following command in the **Command Prompt**

```bash
gradle runExport -Precipe=src\main\resources\executions\examples\reaggregate-traffic-count-to-la.json -Poutput=reaggregate-traffic-count-to-la_output.json
```

- Below are couple of screenshots of start and end of the process.
![Gradle run export 1](https://user-images.githubusercontent.com/19670372/33494879-7ee0a3bc-d6bc-11e7-81f6-c005e6604e92.png)
![Gradle run export 2](https://user-images.githubusercontent.com/19670372/33494878-7ec5c844-d6bc-11e7-92cc-0296a925ebea.png)

##### View output in a file

```bash
start notepad reaggregate-traffic-count-to-la_output.json
```

##### View output in a Folder

```bash
start .
```
