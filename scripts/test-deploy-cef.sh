#/!bin/sh



WILDFLY_HOME="wildfly-11.0.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"


#rm -rf "$FOLDER_DEPLOY/$MEPS_UNZIP"
#unzip -q plugin-meps-*.zip -d $MEPS_UNZIP


cp ../../plugins/Laurentius-cef-plugin/target/plugin-cef.war $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/


cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-demo.sh --init -d mb-laurentius.si
