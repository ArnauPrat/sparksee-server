#!/bin/bash

(cd `dirname $0`/../gremlin-server/ && bin/sparksee-server.sh $@)
