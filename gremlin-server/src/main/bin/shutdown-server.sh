#!/bin/bash

query="{\"gremlin\":\"g.shutdown()\",\"bindings\":{}}"

curl -X POST -H "Accept: application/json"  -d $query http://localhost:8182
