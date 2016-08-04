#!/bin/bash

unset http_proxy
unset https_proxy
unset no_proxy

service filebeat start

java -Djava.security.egd=file:/dev/./urandom -jar /app.jar

#exec "$@"
