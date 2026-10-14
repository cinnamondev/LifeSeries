#!/bin/bash

mkdir ./server/plugins/LifeSeries/
cp ./src/main/resources/config.yml ./server/plugins/LifeSeries/config.yml
# copy plugin
cp $(ls -d ./target/LifeSeries* | head -1 ) ./server/plugins/

