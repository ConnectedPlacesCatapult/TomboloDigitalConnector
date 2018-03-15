# Installation guide for Ubuntu

## Install Git
    sudo apt-get install git

## Install JDK 8
    sudo apt-get install default-jdk

## Install postgreSQL
    sudo apt-get update
    sudo apt-get install postgresql postgresql-contrib

## Install postGIS
    sudo apt-get install postgis

## Install Gradle
    sudo apt-get install gradle

## Clone the repository
    mkdir Tombolo
    cd Tombolo
    git clone https://github.com/FutureCitiesCatapult/TomboloDigitalConnector.git
    cd TomboloDigitalConnector

## Start database Server
    sudo /etc/init.d/postgresql start

## Setup Database
 ### Important: For a correct use of the DC both databases are mandatory
	/* Main Database */
	sudo -i -u postgres
	createuser tombolo
	createdb -O tombolo tombolo -E UTF8
	psql -d tombolo -c "CREATE EXTENSION postgis;"
	psql -d tombolo -c "SET NAMES 'UTF8';"
	exit
	psql -d tombolo -U tombolo < src/main/resources/sql/create_database.sql

	Error:
	Incase of error "Peer authentication failed for tombolo". Please follow these steps:
	1) Open new terminal window
	2) cd /etc/postgresql/<Version>/main  
	3) sudo nano pg_hba.conf
	4) Look for line "local   all	postgres	peer"
	5) Once found create new line under it and write the following under the same words "local   all   tombolo   trust"
	6) Once done press ctrl + x to exit and then Y to save
	7) sudo service postgresql restart
	8) Rerun the last command before error

	/* Test Database
	 * NOTE: this database is not optional, if you do not have it your tests will fail.
	 */
	sudo -i -u postgres
	createuser tombolo_test
	createdb -O tombolo_test tombolo_test -E UTF8
	psql -d tombolo_test -c "CREATE EXTENSION postgis;"
	psql -d tombolo_test -c "SET NAMES 'UTF8';"
	exit
	psql -d tombolo_test -U tombolo_test < src/main/resources/sql/create_database.sql

	Error:
	Incase of error "Peer authentication failed for tombolo_test". Please follow these steps:
	1) Open new terminal window
	2) cd /etc/postgresql/<Version>/main 
	3) sudo nano pg_hba.conf
	4) Look for line "local   all	postgres	peer"
	5) Once found create new line under it and write the following under the same words "local   all   tombolo_test   trust"
	6) Once done press ctrl + x to exit and then Y to save
	7) sudo service postgresql restart
	8) Rerun the last command before error

## Set password for database users
    /* Test Database */
    psql -d tombolo_test -U tombolo_test
    alter user tombolo_test with password 'tombolo_test';
    \q
    /* Main Database */
    psql -d tombolo -U tombolo
    alter user tombolo with password 'tombolo';
    \q

## Rename project files
    mv gradle.properties.example gradle.properties
    mv apikeys.properties.example apikeys.properties

## Build TomboloDigitalConnector
    gradle clean build

## Run an Example
    gradle runExport -Precipe='src/main/resources/executions/examples/reaggregate-traffic-count-to-la.json' -Poutput='reaggregate-traffic-count-to-la_output.json'

## View the output file
    gedit reaggregate-traffic-count-to-la_output.json

## View output file in a folder
    nautilus .
