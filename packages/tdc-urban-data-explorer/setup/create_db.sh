#!/bin/bash

# Run in terminal from project root:
# chmod +x ./setup/create_db.sh
# ./setup/create_db.sh

echo "---------------------------------------------------"
echo "Creating Tombolo City Data Explorer Database"
echo "---------------------------------------------------"

# Delete database and user if they already exist
dropdb tombolo_cde --if-exists
dropuser tombolo_cde --if-exists

# Main database setup
# Create a user and database
createuser tombolo_cde
createdb -O tombolo_cde tombolo_cde -E UTF8
psql -d tombolo_cde -c "CREATE EXTENSION postgis;"
psql -d tombolo_cde -c "CREATE EXTENSION \"uuid-ossp\";"
psql -d tombolo_cde -c "SET NAMES 'UTF8';"

# Restore db dump
pg_restore --username=tombolo_cde --no-privileges --dbname=tombolo_cde --no-owner -v ./db/db.dump

echo "---------------------------------------------------"
echo "Verifying Tombolo City Data Explorer Database"
echo "---------------------------------------------------"

psql -h localhost tombolo_cde -c "\dt"
