Production Notes For AWS Deployment
===================================

Building the Docker image
-------------------------

This procedure is based on the automated CI script from bitbucket-pipelines.yml and is designed
to be run using MacOS 10.13.1 (High Sierra) but can be modified for building on Linux.

#### Prerequisites
* Docker v18.03+ ([Docker for Mac](https://www.docker.com/docker-mac))
* Node v8.11.1
* yarn v1.6.0
* Angular CLI 1.7.4
* Build tools for native node modules (e.g. XCode for macOS or build-essential for Ubuntu)

```bash
# Clone a *clean* copy of the GitHub repo
git clone https://github.com/FutureCitiesCatapult/TomboloVisualisationSuite.git cde
cd cde
```

Build the backend server:
```bash
# Server build
yarn
npm run version
npm run build
```

Build the frontend client:
```bash
# Client build
cd client
yarn
npm run build
```

Build the Docker image:
```bash
# Build the docker image
cd ..
docker build -t citydataexplorer .
```

Tag the image and push to DockerHub:

```bash
docker tag citydataexplorer [organization]/citydataexplorer
docker push [organization]/citydataexplorer
```

Set up AWS Aurora PostgreSQL DB
-------------------------------

The production DB is an AWS Aurora PostgresSQL DB. From the AWS console, navigate to Amazon RDS.
Launch a new Aurora DB instance and select the 'PostgreSQL-compatible' edition. The smallest instance type
(db.r4.large) should be sufficient. You may wish to create a replica in a different availability zone although
this is not necessary for a non-critical demo instance.

Enter a name for the DB and choose a master username and password.

The 'advanced settings' may be left at their default values. By default the DB will be accessible via
a public IP address. Modify the security group to allow access to port 5432 from anywhere. 
This facilitates setup of the DB but you may subsequently want to 
restrict access to the DB once setup is complete.

Follow the setup procedure outlined in create_db.sh to create the default user, database, add required
extensions and restore the database dump.

To target the remote AWS database use the -h (host) and -U (user) options. e.g.:

```bash
createuser -h [remote host] -U [master username] -d -P tombolo_cde
createdb -h [remotedb] -U tombolo_cde tombolo_cde -E UTF8
psql -h [remotedb] -U [master username] -d tombolo_cde -c "CREATE EXTENSION postgis;"
psql -h [remotedb] -U [master username] -d tombolo_cde -c "CREATE EXTENSION \"uuid-ossp\";"

# Restore db dump
pg_restore -h [remotedb] -U tombolo_cde --username=tombolo_cde --no-privileges --dbname=tombolo_cde --no-owner -v ./db/db.dump
```

Set up EC2 Instance and install Docker
--------------------------------------

Create an EC2 instance with this configuration:
* Ubuntu Server 16.04 LTS (HVM), SSD Volume Type (ami-f90a4880)
* t2.medium
* 16GB SSD storage

Configure security groups to allow access from anywhere for ports:
* 22 (SSH)
* 80 (HTTP)
* 443 (HTTPS)

Verify you can SSH into the instance:
```
# SSH into EC2 instance
ssh -i ~/.ssh/[key].pem ubuntu@[EC2 address]
```

Install Docker:
```bash
# Install Docker using get.docker.com script
curl -fsSL get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker ubuntu
docker --version
```

Exit and reconnect SSH for the group membership to take effect.

Install Docker Compose:

```bash
sudo curl -L https://github.com/docker/compose/releases/download/1.21.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

Set up configuration and external data
-------------------------------------

In addition to the docker image there are several components that need to be copied to the EC2 instance:
* docker-compose config file
* NGINX config file
* mbtiles file
* Static map assets (fonts, sprites)

Copy the `docker` folder from the GIT repo to the EC2 box:
```bash
scp -i [key].pem -r ./docker ubuntu@[ec2 public address]:~
```
This includes the config files, map sprites and compressed map fonts but *not* the mbtiles file (this file cannot be distributed).

Copy the mbtiles file `2017-07-03_europe_great-britain.mbtiles` into place:
```bash
scp -i [key].pem ./data/mbtiles/* ubuntu@[ec2 public address]:~/docker/data/mbtiles
```

Unarchive the map fonts:

```bash
# Unarchive fonts on EC2 instance
cd ~/docker/data/static
tar zxvf fonts.tar.gz
```

You should end up with something like this:

```
+-docker
   |
   +-docker-compose.yml
   |
   +-config
   |   |
   |   +-nginx
   |       |
   |       +-citydataexplorer.conf
   +-data
      |
      +-mbtiles
      |   |
      |   +-2017-07-03_europe_great-britain.mbtiles
      |
      +-static
         |
         +-fonts
         |   |
         |   +-metropolis
         |   |
         |   + ...
         |
         +-sprites
             |
             +-darkmatter.json
             |
             + ...
   
```

First Test Run
--------------

Edit docker-compose.yml with the `nano` text editor:
```bash
nano docker-compose.yml
```

Set the following items:
* Docker image name (the default is set to emuanalytics/citydataexplorer)
* Database connection parameters
* SMTP server parameters

Also set the following to the public URL of the EC2 box:
```
- SERVER_BASE_URL=http://[EC2 address] # Public url of server
- SERVER_MAP_ASSETS_URL=http://[EC2 address]/static # Public url of static map assets
```

Run the backend using docker-compose:
```bash
docker-compose up
```

Verify that the DB and SMTP servers connect OK.

Verify that you can access the app at `http://[EC2 address]`

Configuration of Load Balancer and HTTPS
----------------------------------------

Once you have verified that the remote app is working when directly connected you can setup a
load balancer in  front of the app and configure HTTPS.

* Go to the EC2 dashboard
* Create an 'application load balancer'
* Add listener for HTTP (port 80) and HTTPS (port 443)
* Attach certificate to HTTPS listener
* Configure security group to allow access on 80 and 443
* Create load balancer target group with destination port 80 and add EC2 instance to target group
* Configure DNS to point to load balancer
* Configure docker-compose.yml and citydataexplorer.conf to use public URL
* Uncomment section in citydataexplorer.conf to redirect HTTP to HTTPS

Start the backend in daemon mode:
```bash
docker-compose up -d
```

Verify that app works at URL https://[public url]







