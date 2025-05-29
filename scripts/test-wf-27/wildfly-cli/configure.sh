#!/usr/bin/env bash


# The location where the Wildfly instance is installed
JBOSS_HOME=/waso/warpsource/as4-mail/scripts/test-wf-27/wildfly-27.0.0.Final
SERVER_CONFIG=standalone.xml
AM_VERSION=2.1.0-SNAPSHOT
MODULE_PATH=/waso/warpsource/as4-mail/scripts/test-deploy/Laurentius-test/modules/si/laurentius/main/


 {
  echo "JBOSS_HOME=${JBOSS_HOME}"
  echo "SERVER_CONFIG=${SERVER_CONFIG}"
  echo "AM_VERSION=${AM_VERSION}"
  echo "MODULE_PATH=${MODULE_PATH}"
} > env.properties

echo "###################################################################################"
cat env.properties
echo "###################################################################################"
echo "-------------- Configure Wildfly"
${JBOSS_HOME}/bin/jboss-cli.sh --file=resources/configuration.cli --properties=env.properties --resolve-parameter-values

echo "-------------- Clean"
rm env.properties
