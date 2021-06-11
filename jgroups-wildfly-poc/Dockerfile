FROM fhirfactory/pegacorn-base-docker-java:1.0.0

# Default to UTF-8 file.encoding from https://github.com/woahbase/alpine-wildfly/blob/master/Dockerfile_x86_64
ENV LANG C.UTF-8

# START equivalent of https://github.com/jboss-dockerfiles/wildfly/blob/20.0.1.Final/Dockerfile

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 22.0.1.Final
ENV WILDFLY_SHA1 624bd3ca7e66accf5494028f5ebabcb119339803
ENV JBOSS_HOME=${HOME}/wildfly

# Switch back to root
USER root

# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN cd $HOME \
    && wget -O wildfly-$WILDFLY_VERSION.tar.gz https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz \
    && sha1sum wildfly-$WILDFLY_VERSION.tar.gz | grep $WILDFLY_SHA1 \
    && tar xf wildfly-$WILDFLY_VERSION.tar.gz \
    && mv $HOME/wildfly-$WILDFLY_VERSION $JBOSS_HOME \
    && rm wildfly-$WILDFLY_VERSION.tar.gz \
    && chown -R jboss:0 ${HOME} \
    && chmod -R g+rw ${HOME}
# Changing the permissions on $HOME instead of the sub folder $JBOSS_HOME to fix the error "Unable to create index directory: target/lucenefiles"

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

# Switch to jboss user
USER jboss

# Expose the ports we're interested in
EXPOSE 8080 7800 7810 9990


# END equivalent of https://github.com/jboss-dockerfiles/wildfly/blob/20.0.1.Final/Dockerfile

# From https://forums.docker.com/t/how-can-i-view-the-dockerfile-in-an-image/5687/3
# the ConatinerConfig.Cmd in the output of the command > docker inspect wildflyext/wildfly-camel was
# CMD ["/usr/libexec/s2i/run"], but to use the embedded ActiveMQ Artemis and to 
# support jBPM/KIE Server we need to start in full mode as mentioned on:
# https://www.codelikethewind.org/2017/08/08/how-to-embed-a-jbpm-process-in-a-java-ee-application/
# http://www.mastertheboss.com/jboss-jbpm/jbpm6/running-rules-on-wildfly-with-kie-server
# https://github.com/jboss-dockerfiles/drools/blob/master/kie-server/showcase/etc/start_kie-server.sh
# so followed the example of https://github.com/jemella/microbpm-fabric8/blob/master/microbpm-kie-server/src/main/docker/Dockerfile
# and swapped the config files, so if the server is manually restarted from the command line the default
# configuration is for the full-ha mode:
RUN mv $JBOSS_HOME/standalone/configuration/standalone.xml $JBOSS_HOME/standalone/configuration/standalone.xml.orig && \
    cp $JBOSS_HOME/standalone/configuration/standalone-full-ha.xml $JBOSS_HOME/standalone/configuration/standalone.xml

# Copy and run cli to modify the standalone.xml configuration.
# COPY cli/clustering-configuration.cli $JBOSS_HOME/bin/clustering-configuration.cli

# After running each command the content of the "$JBOSS_HOME/standalone/configuration/standalone_xml_history/current" directory
# needs to be deleted as each steps expects it to be empty.  Maybe there is another way??
# RUN $JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/clustering-configuration.cli && \
#     rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current/*

# JGroups clustering config files
RUN mkdir -p $JBOSS_HOME/jgroups-config
RUN chmod -R 777 $JBOSS_HOME/jgroups-config

# deploy the application
COPY target/*.war $JBOSS_HOME/standalone/deployments/

COPY setup-env-then-start-wildfly-as-jboss.sh /
COPY start-wildfly.sh /

ARG IMAGE_BUILD_TIMESTAMP
ENV IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}
RUN echo IMAGE_BUILD_TIMESTAMP=${IMAGE_BUILD_TIMESTAMP}

USER root
# Install gosu based on
# 1. https://gist.github.com/rafaeltuelho/6b29827a9337f06160a9
# 2. https://github.com/tianon/gosu
# 3. https://github.com/tianon/gosu/releases/download/1.12/gosu-amd64
COPY gosu-amd64 /usr/local/bin/gosu
RUN chmod +x /usr/local/bin/gosu && \
	chmod +x /setup-env-then-start-wildfly-as-jboss.sh && \
   	chmod +x /start-wildfly.sh

CMD	["/setup-env-then-start-wildfly-as-jboss.sh"]
