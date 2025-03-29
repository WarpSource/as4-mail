FROM quay.io/wildfly/wildfly:31.0.0.Final-jdk17

ENV VERSION 2.1.0-SNAPSHOT
ENV SERVER_VERSION wildfly-w31.1
ENV LAU_PROJECT .
ENV WILDFLY_HOME /opt/jboss/wildfly



# si.laurentius module libralies
COPY  $LAU_PROJECT/scripts/releases/Laurentius-*.zip $WILDFLY_HOME/Laurentius.zip


user root

RUN yum-config-manager --save --setopt=skip_if_unavailable=true \
    &&  yum  install skip_if_unavailable=1 -y wget unzip  \
    && rm -rf /var/cache/yum

RUN cd $WILDFLY_HOME/ \
    &&  unzip  Laurentius.zip \
    && mv Laurentius-* laurentius-deployment \
    && cd $WILDFLY_HOME/laurentius-deployment/wildfly-31.0 \
    && ls -ltr \
    && ./deploy-laurentius.sh --init -s "${WILDFLY_HOME}" \
    && cd "$WILDFLY_HOME/bin" \
    && chown -R jboss:jboss $WILDFLY_HOME/

VOLUME /opt/jboss/wildfly/standalone/data/laurentius-home
EXPOSE 8080 8081 8443 9990

user jboss
    

# Set the default command to run on boot
CMD ["/opt/jboss/wildfly/bin/laurentius-init.sh", "-d", "test.as4mail.com", "-f"]
