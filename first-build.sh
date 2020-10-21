#!/usr/bin/env bash

sudo apt update -y
sudo apt install -y git

git clone git@github.com:zzl-ffzg/hr-marcell-pipeline.git
#git clone https://github.com/LavaScornedOven/xlike_hr.git hr-marcell-pipeline
cd hr-marcell-pipeline

./setup.sh

git lfs pull

docker-compose build
