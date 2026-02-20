#!/usr/bin/env bash
cd ../
mvn -s config/settings.xml clean package -P mqr-live
