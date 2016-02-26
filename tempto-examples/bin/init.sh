#!/bin/bash

docker run --name tempto-examples-psql -p 15432:5432 -e POSTGRES_USER=blah -e POSTGRES_PASSWORD=blah  -d postgres
docker run --name tempto-examples-psql2 -p 15433:5432 -e POSTGRES_USER=blah -e POSTGRES_PASSWORD=blah  -d postgres
#presto-devenv presto build-image
