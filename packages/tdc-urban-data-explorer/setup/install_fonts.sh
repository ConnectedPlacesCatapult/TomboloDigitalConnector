#!/bin/bash

# Run in terminal from project root:
# chmod +x ./setup/install_fonts.sh
# ./setup/install_fonts.sh

echo "---------------------------------------------------"
echo "Installing map fonts"
echo "---------------------------------------------------"

git clone https://github.com/openmaptiles/fonts
cd fonts
yarn install
node ./generate.js
mv _output ../client/src/assets/fonts
cd ..
rm -rf fonts

echo "---------------------------------------------------"
echo "Installed map fonts"
echo "---------------------------------------------------"

ls ./client/src/assets/fonts
