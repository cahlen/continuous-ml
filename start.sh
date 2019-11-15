#!/usr/bin/env bash
docker-compose up -d
docker exec pyspark-notebook /bin/bash -c 'pip install jip; pip install mleap'
docker logs pyspark-notebook
docker logs -f pyspark-notebook
