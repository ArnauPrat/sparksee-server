#!/usr/bin/env bash


sed  "1 i host: $HOSTNAME" /home/root/sparksee/docker/gremlin-server-rest-sparksee.yaml > /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/gremlin-server-rest-sparksee.yaml

cp /home/root/sparksee/docker/sparksee-empty.properties /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/sparksee-empty.properties

echo "sparksee.license=$SPARKSEE_LICENSE" > /home/root/sparksee/docker/sparksee.cfg

cp /home/root/sparksee/docker/sparksee.cfg /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/sparksee.cfg

cd /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone

echo "starting the gremlin server"

cat $GREMLIN_SERVER_HOME/conf/gremlin-server-rest-sparksee.yaml

gremlin-server.sh $GREMLIN_SERVER_HOME/conf/gremlin-server-rest-sparksee.yaml

