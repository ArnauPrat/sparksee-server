#!/bin/bash
file="$1"
locale="$2"
query="{\"gremlin\":\"g.runScript(script,locale)\",\"bindings\":{\"script\":\"$file\",\"locale\":\"$locale\"}}"
echo $query

curl -X POST -H "Accept: application/json"  -d $query http://$HOSTNAME:8182
