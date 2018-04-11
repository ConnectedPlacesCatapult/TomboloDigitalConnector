# Installation guide for macOS

## Open Terminal
	Press cmd + spacebar
	Type Terminal and hit enter

## Install brew
    /usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

## Install Git
	brew install git

## Install JDK 8
	brew cask install caskroom/versions/java8

## Install PostgreSQL
	brew install postgres

## Install PostGIS
	brew install postgis

## Install Gradle
	brew install gradle

## Clone the repository
	mkdir Tombolo
	cd Tombolo
	git clone https://github.com/FutureCitiesCatapult/TomboloDigitalConnector.git
	cd TomboloDigitalConnector

## Start Database Server
	pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start

## Setup Databases
 ### Important: For a correct use of the DC both databases are mandatory
	/* Main Database */
	createuser tombolo
	createdb -O tombolo tombolo -E UTF8
	psql -d tombolo -c "CREATE EXTENSION postgis;"
	psql -d tombolo -c "SET NAMES 'UTF8';"
	psql -d tombolo -U tombolo < src/main/resources/sql/create_database.sql

	/* Test Database
	 * NOTE: this database is not optional, if you do not have it your tests will fail.
	 */
	createuser tombolo_test
	createdb -O tombolo_test tombolo_test -E UTF8
	psql -d tombolo_test -c "CREATE EXTENSION postgis;"
	psql -d tombolo_test -c "SET NAMES 'UTF8';"
	psql -d tombolo_test -U tombolo_test < src/main/resources/sql/create_database.sql

 ### Set password for database users
	/* Test Database */
	psql -d tombolo_test -U tombolo_test
   	alter user tombolo_test with password 'tombolo_test';
   	\q
	/* Main Database */
    psql -d tombolo -U tombolo
   	alter user tombolo with password 'tombolo';
   	\q

## Rename project files
    cp gradle.properties.example gradle.properties
    cp apikeys.properties.example apikeys.properties

## Build TomboloDigitalConnector
	gradle clean build

## Run an Example
    gradle runExport -Precipe='src/main/resources/executions/examples/reaggregate-traffic-count-to-la.json' -Poutput='reaggregate-traffic-count-to-la_output.json'


## View output file
	open reaggregate-traffic-count-to-la_output.json

## View output file in a folder
	open .

