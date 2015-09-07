#!/usr/bin/env bash

mkdir -p $HOME/.m2
touch $HOME/.m2/settings.xml

echo "<settings><servers>" >> $HOME/.m2/settings.xml
echo "<server><id>adapt03-libs</id><username>" >> $HOME/.m2/settings.xml
echo $MVN_USER >> $HOME/.m2/settings.xml
echo "</username><password>" >> $HOME/.m2/settings.xml
echo $MVN_PWD >> $HOME/.m2/settings.xml
echo "</password></server>" >> $HOME/.m2/settings.xml


echo "<server><id>adapt03</id><username>" >> $HOME/.m2/settings.xml
echo $MVN_USER >> $HOME/.m2/settings.xml
echo "</username><password>" >> $HOME/.m2/settings.xml
echo $MVN_PWD >> $HOME/.m2/settings.xml
echo "</password></server>" >> $HOME/.m2/settings.xml


echo "<server><id>adapt03-coherentpaas-snapshots</id><username>" >> $HOME/.m2/settings.xml
echo $MVN_USER >> $HOME/.m2/settings.xml
echo "</username><password>" >> $HOME/.m2/settings.xml
echo $MVN_PWD >> $HOME/.m2/settings.xml
echo "</password></server>" >> $HOME/.m2/settings.xml
echo "</servers></settings>" >> $HOME/.m2/settings.xml

echo "building the project"

cd /home/root/sparksee && mvn package -DskipTests

echo "copying the standlalone into the working dir"

cp -R /home/root/sparksee/gremlin-server/* /usr/local/sparksee

sed  "1 i host: $HOSTNAME" /home/root/sparksee/docker/gremlin-server-rest-sparksee.yaml > /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/gremlin-server-rest-sparksee.yaml

cp /home/root/sparksee/docker/sparksee-empty.properties /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/sparksee-empty.properties

echo "sparksee.license=$SPARKSEE_LICENSE" > /home/root/sparksee/docker/sparksee.cfg

cp /home/root/sparksee/docker/sparksee.cfg /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/sparksee.cfg

export GREMLIN_SERVER_HOME=/usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone

export PATH=$PATH:$GREMLIN_SERVER_HOME/bin

cd /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone

echo "starting the gremlin server"

cat $GREMLIN_SERVER_HOME/conf/gremlin-server-rest-sparksee.yaml

gremlin-server.sh $GREMLIN_SERVER_HOME/conf/gremlin-server-rest-sparksee.yaml

