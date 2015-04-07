#!/bin/bash
export SPARKSEE_GREMLIN=$HOME/.m2/repository/com/tinkerpop/sparksee-gremlin/3.0.0-SNAPSHOT/sparksee-gremlin-3.0.0-SNAPSHOT.jar
export SPARKSEE=$HOME/.m2/repository/com/sparsity/sparkseejava/5.1.1/sparkseejava-5.1.1.jar
export CLASSPATH=$SPARKSEE:$SPARKSEE_GREMLIN:$CLASSPATH
echo $CLASSPATH
(cd `dirname $0`/../target/gremlin-server-*-standalone/ && bin/gremlin-server.sh $@)
