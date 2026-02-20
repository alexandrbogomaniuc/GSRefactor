#!/usr/bin/env bash
cd ../
mvn -s config/settings.xml clean package -P siberteam-stress
#mvn -Dmaven.test.skip=true -s config/settings.xml clean package -P development