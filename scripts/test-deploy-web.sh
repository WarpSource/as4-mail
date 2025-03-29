#/!bin/sh



WILDFLY_HOME="wildfly-31.0.1.Final"
BOUNDLE_TEST="Laurentius-test"
FOLDER_DEPLOY="test-deploy"

rm -rf $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/laurentius-web.*

# artefact deployment
cp ../Laurentius-app/Laurentius-web/target/laurentius-web.war $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/
# hot deploy
# rm $FOLDER_DEPLOY/$WILDFLY_HOME/standalone/deployments/laurentius-web.war
# ln  -s /waso/warpsource/as4-mail/Laurentius-app/Laurentius-web/target/laurentius-web /waso/warpsource/as4-mail/scripts/test-deploy/wildfly-31.0.1.Final/standalone/deployments/laurentius-web.war

cd "$FOLDER_DEPLOY/$WILDFLY_HOME/bin"

./laurentius-init.sh --init -d mb-laurentius.si
