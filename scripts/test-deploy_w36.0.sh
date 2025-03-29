#!/usr/bin/env bash

JAVA_HOME=/opt/java/jdk-17.0.10/
WILDFLY_FOLDER_NAME="wildfly-36.0.1.Final"
WILDFLY_SETTINGS="wildfly-36.0"

FOLDER_DEPLOY="test-deploy"
BUNDLE_NAME="Laurentius-test"
INIT_DOMAINS="test-as4mail.com"
#INIT_DOMAINS="test-as4mail.com,mb-laurentius.si"

export DEBUG_MODE=true
export "JAVA_HOME=/opt/java/jdk-17.0.10/"

echo  "Cleaning up old deployment..."
rm -rf "${FOLDER_DEPLOY:?"Deploy folder must not be empty"}/${WILDFLY_FOLDER_NAME:?"Wildfly folder name not be empty"}"
echo  "Unzip wildfly to [${FOLDER_DEPLOY}]"
unzip -q "../../settings/${WILDFLY_FOLDER_NAME}.zip" -d ${FOLDER_DEPLOY}
WILDFLY_HOME="$(readlink -f "${FOLDER_DEPLOY}/${WILDFLY_FOLDER_NAME}/")"

echo  "Deploy laurentius settings [${WILDFLY_SETTINGS}]"
cd "${FOLDER_DEPLOY}/${BUNDLE_NAME}/${WILDFLY_SETTINGS}" \
  ||  { echo "Folder '${FOLDER_DEPLOY}/${BUNDLE_NAME}/${WILDFLY_SETTINGS}' not exist!"; exit 101;}
./deploy-laurentius.sh --init -s "${WILDFLY_HOME}"

echo  "Initialize laurentius with domains [${INIT_DOMAINS}] from [${WILDFLY_HOME}/bin]"
cd "${WILDFLY_HOME}/bin" || { echo "Warning: Folder [${WILDFLY_HOME}/bin] does not exist!"; exit 102; }

./laurentius-init.sh --init -d "${INIT_DOMAINS}" -f
