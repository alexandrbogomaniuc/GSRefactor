#!/usr/bin/env bash
cd ../
mvn -s config/settings.xml clean package -P mqb2-live
