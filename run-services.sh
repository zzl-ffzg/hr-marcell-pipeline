#!/usr/bin/env bash

java -jar /service/xlike_hr.jar &> /service/log/xlike_hr.log &

until nc -z localhost $XLIKE_PORT; do sleep 1; echo "Waiting for XLIKE to start up..."; done

PEX_SCRIPT=gunicorn /service/annotator.pex annotator.application:app -b :${ANNOTATOR_PORT:-8080} 2>&1 | tee /service/log/annotator.log
