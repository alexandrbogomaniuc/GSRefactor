#!/bin/sh

echo "Searching for prohibited brand words."

SUPPORTED_FILE_EXTENSIONS='.*\.\(xml\|properties\|jsp\|html\|css\|js\)$'
SCRIPT_PATH="`dirname \"$0\"`"
ROOT_WAR="${SCRIPT_PATH}/../web-gs/target/ROOT.war"
BRAND=${1}

if [ "${BRAND}" = "nucleus" ]
then
    PROHIBITED_BRAND_WORDS="betsoft\|bsg\|discreetgaming\|digitus"
else
    PROHIBITED_BRAND_WORDS="nucleus"
fi

TEMP_DIR=$(mktemp -d)
TEMP_ROOT_DIR="${TEMP_DIR}/ROOT"

unzip -q ${ROOT_WAR} -d ${TEMP_ROOT_DIR}

if find ${TEMP_ROOT_DIR} ! -path "${TEMP_ROOT_DIR}/META-INF/*" \
    ! -path "${TEMP_ROOT_DIR}/WEB-INF/classes/ClusterConfiguration.xml" \
    -iregex ${SUPPORTED_FILE_EXTENSIONS} -print0 \
    | xargs -r0 grep -o -e ${PROHIBITED_BRAND_WORDS}
then
    echo "ERROR: Found prohibited brand words in ROOT.war, war has been removed."
    rm ${ROOT_WAR}
    rm -rf ${TEMP_DIR}
    exit 1
fi

rm -rf ${TEMP_DIR}

echo "Searching completed."


