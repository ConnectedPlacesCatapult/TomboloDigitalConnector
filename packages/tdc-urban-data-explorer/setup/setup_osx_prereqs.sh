#!/bin/bash
# Setup script assumes brew is already installed

# Run in terminal from project root:
# chmod +x ./setup/setup_osx.sh
# ./setup/setup_osx.sh

echo "---------------------------------------------------"
echo "Installing Tombolo City Data Explorer Prerequisites"
echo "---------------------------------------------------"

echo "----------------------------------"
echo "Installing node v8"
echo "----------------------------------"
brew install node@8
brew link --force node@8

echo "----------------------------------"
echo "Installing yarn"
echo "----------------------------------"
brew install yarn --without-node

echo "----------------------------------"
echo "Installing Angular CLI"
echo "----------------------------------"
npm install -g @angular/cli

echo "----------------------------------"
echo "Installing Maildev"
echo "----------------------------------"
npm install -g maildev

echo "----------------------------------"
echo "Installing PostgreSQL"
echo "----------------------------------"
brew install postgresql

echo "----------------------------------"
echo "Installing PostGIS"
echo "----------------------------------"
brew install postgis

echo "----------------------------------"
echo "Verifying installation"
echo "----------------------------------"
echo node: `node -v`
echo yarn: `yarn -v`
echo maildev: `maildev --version`
echo postgresql: `psql --version`
echo GDAL: `ogr2ogr --version`
