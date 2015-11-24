#!/bin/bash

case `uname` in
  CYGWIN*)
    CP="`dirname $0`"/../config/
    CP="$CP":$( echo `dirname $0`/../lib/*.jar . | sed 's/ /;/g')
    CP="$CP":$( echo `dirname $0`/../ext/*.jar . | sed 's/ /;/g')
    ;;
  *)
    CP="`dirname $0`"/../config/
    CP="$CP":$( echo `dirname $0`/../lib/*.jar . | sed 's/ /:/g')
    CP="$CP":$( echo `dirname $0`/../ext/*.jar . | sed 's/ /;/g')
esac
#echo $CP

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
CP=$CP:$(find -L $DIR/../ext/ -name "*.jar" | tr '\n' ':')

export CLASSPATH="${CLASSPATH:-}:$CP"

# Find Java
if [ "$JAVA_HOME" = "" ] ; then
    JAVA="java -server"
else
    JAVA="$JAVA_HOME/bin/java -server"
fi

# Set Java options
if [ "$JAVA_OPTIONS" = "" ] ; then
    JAVA_OPTIONS="-Xms32m -Xmx512m"
fi

if [ "$GREMLIN_SERVER_HOME" = "" ] ; then
    $GREMLIN_SERVER_HOME=pwd
fi

#export BIB_DEBUG_OPTS="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=1047"

# Execute the application and return its exit code
if [ "$1" = "-i" ]; then
  shift
  exec $JAVA $BIB_DEBUG_OPTS -Dlog4j.configuration=file:$GREMLIN_SERVER_HOME/conf/log4j-server.properties $JAVA_OPTIONS -cp $CP:$CLASSPATH com.tinkerpop.gremlin.server.util.GremlinServerInstall "$@"
else
  exec $JAVA $BIB_DEBUG_OPTS -Dlog4j.configuration=file:$GREMLIN_SERVER_HOME/conf/log4j-server.properties $JAVA_OPTIONS -cp $CP:$CLASSPATH com.tinkerpop.gremlin.server.GremlinServer "$@"
fi
