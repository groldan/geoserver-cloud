FROM org.geoserver.cloud/gs-cloud-docker-base-java:latest

ARG JAR_FILE=dummy
ADD target/${JAR_FILE} /opt/app/restconfig-service.jar

#data directory, remove once we get rid of it
ARG DATADIR=target/release.tar.gz
ADD ${DATADIR} /tmp/data_directory
RUN sudo chown -R geo:geo /tmp/data_directory
ENV GEOSERVER_DATA_DIR=/tmp/data_directory

CMD exec java $JAVA_OPTS -jar /opt/app/restconfig-service.jar