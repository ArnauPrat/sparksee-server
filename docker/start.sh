#!/usr/bin/env bash

if [ -n "$XRAYHOST" ]; then sed  "s/REPLACE_XRAY_HOST/${XRAYHOST}/g" /home/root/sparksee/docker/logback.template.groovy > /usr/local/sparksee/etc/logback.groovy; fi

cd $SPARKSEE_SERVER_HOME && sparksee start -Dsparksee.license=$SPARKSEE_LICENSE -Dsparksee.io.recovery=true

