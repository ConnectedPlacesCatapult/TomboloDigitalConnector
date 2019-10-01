City Data Explorer
==================

Prerequisites
-------------

The following libraries and tools are required to build and run the City Data Explorer (CDE).

- node v8.11.1
- yarn v1.6.0
- PostgreSQL 9.5+
- PostGIS 2.2.4+
- Angular CLI 1.7.4
- Build tools for native node modules (e.g. XCode for macOS or build-essential for Ubuntu)

Full instructions are given below for installing these prerequisites on macOS using the Homebrew package manager. However, the
CDE can also be built and run on Linux by installing the prerequisites using an alternative package manager (such as **apt** on Ubuntu).

A note about node v9: The project will run with node v9, however, the project depends on the
[node-mapnik](https://github.com/mapnik/node-mapnik) native library and a prebuilt binary for node V9 is not currently available. Until the
maintainers of node-mapnik release a node v9-compatible version, the CDE must be built and run with
node v8.

macOS Local Development
------------------------

Shell scripts are provided in `./setup/` for setting up a local development system on macOS 10.13.1 (High Sierra). Set-up should work on
earlier versions of macOS but has not been tested.

Set-up requires that you have the Homebrew package manager installed. If you do not already have this then follow
installation instructions at [Homebrew](https://brew.sh/).

You can verify if you have Homebrew installed by running the following command in a terminal:
```bash
# Check brew is installed
brew --version

# Output on success
# Homebrew 1.6.1
# Homebrew/homebrew-core (git revision 5bd0e; last commit 2018-04-18)
```

The following instructions assume that you have cloned the CDE repository using `git` to
a local folder. The project root folder is referred to as [projectroot] in this README.

### Installation of required libraries and tools

Run the script `setup_osx_prereqs.sh` as shown below to install the required libraries and tools using brew.

```bash
# Install prerequisites
cd [projectroot]
./setup/setup_osx_prereqs.sh

# Output on success
# ----------------------------------
# Verifying installation
# ----------------------------------
# node: v8.11.1
# yarn: 1.6.0
# maildev: 1.0.0-rc3
# postgresql: psql (PostgreSQL) 10.3
# GDAL: GDAL 2.2.4, released 2018/03/19
```

If the installation ran successfully then you will see output similar to that shown above. Please
check the version numbers against the minimum required versions before proceeding.

### Creating the database

To start the PostgreSQL server, use the following command:

```bash
# to start PostgreSQL
pg_ctl -D /usr/local/var/postgres -l /usr/local/var/postgres/server.log start
```

Run the `./setup/create_db.sh` script to create the `tombolo_cde` database, `tombolo_cde` user
and restore a database dump containing the default maps, datasets and other required entities.

```bash
# Create DB, user and restore DB.
./setup/create_db.sh

# Output on success
# ---------------------------------------------------
# Verifying Tombolo City Data Explorer Database
# ---------------------------------------------------
#                          List of relations
# Schema |                 Name                  | Type  |    Owner
# --------+---------------------------------------+-------+-------------
# public | 084f7c4101ed26309ffd1effceb8bb74_data | table | tombolo_cde
# public | 08a714da565c1faf2d28f426fa9148da_data | table | tombolo_cde
# public | 398cac8d481ca9e61c565792ab4909b0_data | table | tombolo_cde
# public | 42e6f1fa29694b4af2d822520ba0e3ac_data | table | tombolo_cde
# public | 81839777b2be37de0389ea5e031847c3_data | table | tombolo_cde
# public | 8dc193dbbf36b11f016120057852ed9d_data | table | tombolo_cde
# public | Sessions                              | table | tombolo_cde
# public | a7a8bc1624ec769d26fe86b643d7f9be_data | table | tombolo_cde
# public | active_transport                      | table | tombolo_cde
# public | barking_dagenham                      | table | tombolo_cde
# public | base_maps                             | table | tombolo_cde
# public | bookmarks                             | table | tombolo_cde
# public | data_attributes                       | table | tombolo_cde
# public | dataset_groups                        | table | tombolo_cde
# public | datasets                              | table | tombolo_cde
# public | e677a5595ed3e47a4b597b215c83184d_data | table | tombolo_cde
# public | file_uploads                          | table | tombolo_cde
# public | map_groups                            | table | tombolo_cde
# public | map_layers                            | table | tombolo_cde
# public | maps                                  | table | tombolo_cde
# public | palettes                              | table | tombolo_cde
# public | social_isolation                      | table | tombolo_cde
# public | spatial_ref_sys                       | table | robin
# public | users                                 | table | tombolo_cde
# (24 rows)

```

If the database creation ran successfully then you will see output similar to that shown above.
Check the list of created tables before proceeding.

Installation of map fonts
-------------------------

The map styles used by the CDE require a set of open source fonts in .pbf format. To install
the fonts from [OpenMapTiles](https://github.com/openmaptiles/fonts) run the command below in a terminal:

```bash
# Install map fonts
./setup/install_fonts.sh

# Output on success
# ---------------------------------------------------
# Installed map fonts
# ---------------------------------------------------
# Metropolis Black		Metropolis Medium		Noto Sans Regular		Open Sans Semibold Italic	Roboto Black Italic		Roboto Light
# Metropolis Black Italic		Metropolis Medium Italic	Open Sans Bold			PT Sans Bold			Roboto Bold			Roboto Light Italic
# Metropolis Bold			Metropolis Regular		Open Sans Bold Italic		PT Sans Bold Italic		Roboto Bold Italic		Roboto Medium
# Metropolis Bold Italic		Metropolis Regular Italic	Open Sans Extra Bold		PT Sans Caption Regular		Roboto Condensed Bold		Roboto Medium Italic
# Metropolis Extra Bold		Metropolis Semi Bold		Open Sans Extra Bold Italic	PT Sans Caption Web Bold	Roboto Condensed Bold Italic	Roboto Regular
# Metropolis Extra Bold Italic	Metropolis Semi Bold Italic	Open Sans Italic		PT Sans Italic			Roboto Condensed Italic		Roboto Thin
# Metropolis Extra Light		Metropolis Thin			Open Sans Light			PT Sans Narrow Bold		Roboto Condensed Light		Roboto Thin Italic
# Metropolis Extra Light Italic	Metropolis Thin Italic		Open Sans Light Italic		PT Sans Narrow Regular		Roboto Condensed Light Italic	_output
# Metropolis Light		Noto Sans Bold			Open Sans Regular		PT Sans Regular			Roboto Condensed Regular
# Metropolis Light Italic		Noto Sans Italic		Open Sans Semibold		Roboto Black			Roboto Italic
```
Running the command above will clone the OpenMapTiles font repository, build the fonts and
move them into position at `[projectroot]/client/src/assets/fonts`.


### Installation of Basemap Tiles

The backend server requires an OpenStreetMap extract covering your area of support (e.g. UK/Planet-wide).
Download the desired extract as an `mbtiles` file from [OpenMapTiles](https://openmaptiles.com/downloads/planet/]).
OSM extracts from OpenMapTiles are free for non-commercial use. Please read the Terms and Conditions thoroughly before using.

For simplicity, the CDE has been pre-configured to use the Great Britain extract.
Download the file from [OpenMapTiles GB MBTiles](https://openmaptiles.com/downloads/tileset/osm/europe/great-britain/). Select
'Open-source or open-data project website' and then click the 'Free download (after you sign in)' button.

The file you receive should be called `2017-07-03_europe_great-britain.mbtiles` and you should move it
into place using the following terminal commands:

```bash
# Install basemap mbtiles file
cd [projectroot]
mkdir -p ./data/mbtiles
mv [download location]/2017-07-03_europe_great-britain.mbtiles ./data/mbtiles

```

Please note that if you download a different region or receive an newer file than the one shown above you will
have to reconfigure the CDE to locate the basemap mbtiles file. Use a database editor or `psql` to update the
`source` field of the dataset with `id` 38080fa1-e5c3-487d-8347-3532d071c8a6.

The pre-configured value is `mbtiles://./data/mbtiles/2017-07-03_europe_great-britain.mbtiles`, which shows how to
configure a path to your mbtiles file relative to `[projectdir]`.

For example, using `psql` to modify the configuration:

```sql
# To configure a newer mbtiles file
update datasets set source = 'mbtiles://./data/mbtiles/2018-XX-XX_europe_great-britain.mbtiles' where id = '38080fa1-e5c3-487d-8347-3532d071c8a6';

# To configure an absolute path
update datasets set source = 'mbtiles:///abs/path/mytiles.mbtiles' where id = '38080fa1-e5c3-487d-8347-3532d071c8a6';

```

### Installing required node modules

To install required node modules for the backend server and front end UI run the following commands in a terminal:

```bash
# Install backend node modules
cd [projectroot]
yarn install

# Install front end client modules
cd client
yarn install

```


### Generate version file

The git commit SHA hash and build date are required to display version information in the app. This information
is contained in the file `[projectroot]/src/version.js`. To generate this file, run the following command in a
terminal.

```bash
# Generate version.js
yarn run version
```

For local development it is sufficient to run this command once during setting up your environment. For continuous
integration or production builds the version file should be regenerated to provide up-to-date version
information to be displayed in the app.

### Running a development SMTP mail server

The CDE requires connection to an SMTP server to run. This is used for sending account sign-up and 
password reset emails. The `setup-osx-prereqs.sh` script above installs a convenient development
SMTP server for local use. You must run the SMTP server before you can run the CDE. In a separate terminal,
run the following command and leave the mail server running.

```bash
# Run maildev mailserver
maildev

# Output on success
# MailDev webapp running at http://0.0.0.0:1080
# MailDev SMTP Server running at 0.0.0.0:1025
```

Email sent to the server can be accessed using the url `http://localhost:1080` in a browser. Any email
address or domain can be used with the maildev SMTP server and does not have to be pre-configured.

The CDE is pre-configured to connect to the SMTP server on port 1025. If you wish to use a different SMTP server then please refer to the configuration section below.

### Running the CDE backend server and front end UI

To run the backend:

```bash
# Run backend server
cd [projectroot]
yarn run dev

# Output on success
# ...
# 2018-04-18T15:29:44.237Z - info: [tombolo-viewer] Connected to DB: tombolo_cde
# 2018-04-18T15:29:44.263Z - info: [tombolo-viewer] Connected to SMTP Server
# ...
```

To run the front end UI (in a separate terminal):

```bash
# Run front end UI
cd client
yarn start

# Output on success
# ** NG Live Development Server is listening on localhost:4200, open your browser on http://localhost:4200/ **
# ...
# chunk {inline} inline.bundle.js (inline) 5.79 kB [entry] [rendered]
# chunk {main} main.bundle.js (main) 1.92 MB [initial] [rendered]
# chunk {polyfills} polyfills.bundle.js (polyfills) 704 kB [initial] [rendered]
# chunk {scripts} scripts.bundle.js (scripts) 3.62 kB [initial] [rendered]
# chunk {styles} styles.bundle.js (styles) 822 kB [initial] [rendered]
# chunk {vendor} vendor.bundle.js (vendor) 23.2 MB [initial] [rendered]
# webpack: Compiled successfully.

```

Once the CDE backend server and frontend UI are running you can access the CDE in your browser at
url `http://localhost:4200`.

Changes to source files are automatically detected during local development 

Configuration
-------------

The CDE is preconfigured to run using the macOS local development instructions above. If your
installation requirements are different then you will need to reconfigure the system.

[node-config](https://github.com/lorenwest/node-config) is used for app configuration. The base configuration
can be found in `config/default.toml`. To change configuration parameters, you can edit this file or override
the default configuration by:
 
 - setting environment variables as specified in `custom-environment-variables.toml`
 - create a local override file `conifg/local.toml`. This is excluded from git commits in `.gitignore`
 
 We strongly advise using environment variables or `local.toml` to specify passwords
for DB connection and SMTP configuration rather than commiting passwords to GitHub.

#### Essential Configuration Parameters

An example `local.toml` file is shown below:

```toml
# Set public URL of host server
[server]
baseUrl = "https://mypublicfacing.dataexplorer.com"

# Configure DB connection
[db]
host = "mydbhost"
port = 5432
username = "tombolo"
password = "tombolo"
database = "tombolo_dev"

# Configure SMTP server
[smtp]
host = 'mysmtpserver.com'
port = 587
secure = false
ignoreTLS = false

#Configure SMTP auth credentials
[smtp.auth]
user = 'mySMTPusername'
pass ='mySMTPpassword'

```

Production
----------

Running this web app in production is beyond the scope of this README. However, we have provided
a `Dockerfile` and example Docker compose files for deploying the app as a Docker container.
You can follow the steps in the file `bitbucket-pipelines.yml` to build a Docker image of
the complete app.

The backend server is generally stateless and can be deployed as a load-balanced cluster of instances.
Stateful sessions are used only for holding login information and session content is persisted in the
backend database.


Documentation
-------------
Documentation is generated with [TypeDoc][1]. Use `/** .. **/`-style doc comments for classes,
function and methods.

To generate documentation:
```bash
npm run gen-doc
```

To view documentationn:
```bash
npm run view-gen
```

[1]: http://typedoc.org/
