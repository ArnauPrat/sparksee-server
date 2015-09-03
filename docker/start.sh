

echo "<settings><servers>" > $HOME/.m2/settings.xml
echo "<server><id>adapt03-libs</id><username>" > $HOME/.m2/settings.xml
echo $MVN_USER > $HOME/.m2/settings.xml
echo "</username><password>" $HOME/.m2/settings.xml
echo $MVN_PWD > $HOME/.m2/settings.xml
echo "</password></server>"


echo "<server><id>adapt03</id><username>" > $HOME/.m2/settings.xml
echo $MVN_USER > $HOME/.m2/settings.xml
echo "</username><password>" $HOME/.m2/settings.xml
echo $MVN_PWD > $HOME/.m2/settings.xml
echo "</password></server>"


echo "<server><id>adapt03-coherentpaas-snapshots</id><username>" > $HOME/.m2/settings.xml
echo $MVN_USER > $HOME/.m2/settings.xml
echo "</username><password>" $HOME/.m2/settings.xml
echo $MVN_PWD > $HOME/.m2/settings.xml
echo "</password></server>"
echo "</servers></settings>"> $HOME/.m2/settings.xml

cd /home/root/sparksee && mvn package

cp -R /home/root/sparksee/gremlin-server /usr/local/sparksee

cp /home/root/sparksee/docker/gremlin-server-rest-sparksee.yaml target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf

cp /home/root/sparksee/docker/sparksee.cfg target/gremlin-server-3.0.0-SNAPSHOT-standalone/conf/sparksee.cfg

export GREMLIN_SERVER_HOME=/usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone

export PATH=$PATH:$GREMLIN_SERVER_HOME/bin

cd /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone

gremlin-server.sh $GREMLIN_SERVER_HOME/conf/gremlin-server-rest-sparksee.yaml

tail -f gremlin-server.log
