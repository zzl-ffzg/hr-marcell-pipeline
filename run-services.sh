#!/usr/bin/env bash

java -jar /service/xlike_hr.jar &> /service/log/xlike_hr.log &

PEX_SCRIPT=gunicorn /service/annotator.pex annotator.application:app -b :${ANNOTATOR_PORT:-8080} 2>&1 | tee /service/log/annotator.log
