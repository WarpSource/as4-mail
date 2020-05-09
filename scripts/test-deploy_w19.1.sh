#/!bin/sh


WILDFLY_HOME="wildfly-19.1.0.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"
BOUNDLE_NAME="Laurentius-test"

rm -rf "$FOLDER_DEPLOY/$WILDFLY_HOME"
unzip -q "../../settings/$WILDFLY_HOME.zip" -d $FOLDER_DEPLOY


cd "$FOLDER_DEPLOY/$BOUNDLE_NAME/wildfly-19.1"

./deploy-laurentius.sh --init -s "../../$WILDFLY_HOME"

cd "../../$WILDFLY_HOME/bin"

./laurentius-init.sh --init -d mb-laurentius.si

