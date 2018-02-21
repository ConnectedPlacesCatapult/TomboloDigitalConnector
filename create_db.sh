#!/bin/bash

#Give this script execution permision 
# chmod +x create_db.sh

# Delete database and user if they already exist
dropdb tombolo
dropdb tombolo_test
dropuser tombolo
dropuser tombolo_test 

# Main database setup
# Create a user and database
createuser tombolo
createdb -O tombolo tombolo -E UTF8
psql -d tombolo -c "CREATE EXTENSION postgis;"
psql -d tombolo -c "SET NAMES 'UTF8';"


# Create DB tables and load initial fixtures
psql -d tombolo -U tombolo < src/main/resources/sql/create_database.sql

# Test database setup
# IMPORTANT NOTE: The tombolo_test database is not optional, if not set up the tests will fail.
# Create a user and database
createuser tombolo_test
createdb -O tombolo_test tombolo_test -E UTF8
psql -d tombolo_test -c "CREATE EXTENSION postgis;"
psql -d tombolo_test -c "SET NAMES 'UTF8';"

# Create DB tables and load initial fixtures
psql -d tombolo_test -U tombolo_test < src/main/resources/sql/create_database.sql
