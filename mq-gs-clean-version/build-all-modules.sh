./install-gsn-casino-project.sh
cd ./rng
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./annotations/
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./kryo-validator/
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./sb-utils/
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./utils/
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./common-promo/
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./common
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ./cassandra-cache/cache/
mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error
cd ..
cd ..