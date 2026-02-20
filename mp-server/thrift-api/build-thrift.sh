#!/bin/sh
thrift -out ./src/main/java/ --gen java:generated_annotations=suppress mp.thrift
#thrift -out ./src/main/java/ --gen java:generated_annotations=suppress bot.thrift
