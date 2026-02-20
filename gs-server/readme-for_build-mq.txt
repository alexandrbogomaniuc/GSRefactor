run install-gsn-casino-project.sh

cd ./rng
run install_rng.sh

cd ./annotations
run mvn install

cd ./kryo-validator
run mvn install

cd ./sb-utils
run install_utils.sh

cd utils
run mvn install

cd ./common-promo
run mvn install

cd ./common
run install-gsn-common.sh

cd ./cassandra-cache/cache
run mvn install

