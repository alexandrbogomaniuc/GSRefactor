#basic script to install jar to local repository
#mvn install:install-file -Dfile=<path-to-file> -DgroupId=<group-id> \
#    -DartifactId=<artifact-id> -Dversion=<version> -Dpackaging=<packaging>

#install tpi-api-1.1-client-src.jar
mvn install:install-file \
-Dfile=./tpi-api-1.1-client-src.jar \
-DgroupId=ca.alea.tpi \
-DartifactId=api \
-Dversion=1.1 \
-Dpackaging=jar


