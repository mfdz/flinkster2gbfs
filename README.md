# Flinkster2GBFS

Flinkster2GBFS creates a GBFS feed via the Deutsche Bahn Flinkster API.



git submodule update --init

Docker 
======

To run flinkster2gbfs via docker, build and run as follows:

```
$ docker build -t flinkster2gbfs .
$ docker run --rm -e FLINKSTER_TOKEN=<your flinkster API token> -e FLINKSTER_LOOP=true -v $(PWD):/data flinkster2gbfs
```
To receive a Flinkster API token, register with the 
[DB OPEN API Portal](https://developer.deutschebahn.com/store/site/pages/getting-started.jag).
and create an API token for the Flinkster_API_NG - v1.

