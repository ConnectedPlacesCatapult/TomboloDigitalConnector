#!/bin/bash
# Script assumes brew is installed and that the user wishes to 
# clone the TDC repo to the current directory.
# To run:
# 1. Clone the repo
# 2. In terminal type ./setup/setup_osx.sh

echo "----------------------------------"
echo "Installing TomboloDigitalConnector"
echo "----------------------------------"

echo "----------------------------------"
echo "Install brew"
echo "----------------------------------"

/usr/bin/ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"

echo "----------------------------------"
echo "Update brew"
echo "----------------------------------"

brew doctor
brew update

echo "----------------------------------"
echo "Installing JDK"
echo "----------------------------------"
brew cask install java

echo "----------------------------------"
echo "Verifying JDK installation"
echo "----------------------------------"
java -version

echo "----------------------------------"
echo "Installing PostgreSQL"
echo "----------------------------------"
brew install postgresql

echo "----------------------------------"
echo "Verifying PostgreSQL installation"
echo "----------------------------------"
psql --version

echo "----------------------------------"
echo "Installing PostGIS"
echo "----------------------------------"
brew install postgis

echo "----------------------------------"
echo "Installing Gradle"
echo "----------------------------------"
brew install gradle

echo "----------------------------------"
echo "Verifying Gradle installation"
echo "----------------------------------"
gradle --version

echo "----------------------------------"
echo "Set up defaults for project"
echo "----------------------------------"
cp gradle.properties.example gradle.properties
cp apikeys.properties.example apikeys.properties

echo "----------------------------------"
echo "Set up database"
echo "----------------------------------"
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
chmod +x setup/create_db.sh
./setup/create_db.sh

echo "----------------------------------"
echo "Test setup"
echo "----------------------------------"
gradle test
