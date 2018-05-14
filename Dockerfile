FROM java
MAINTAINER Lorena Qendro <lqendro@futurecities.catapult.org.uk>
RUN apt-get update && apt-get -y install gradle postgresql-client
