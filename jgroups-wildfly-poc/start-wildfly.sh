#!/usr/bin/env bash
# NOTE: this file should have Unix (LF) EOL conversion performed on it to avoid: "env: can't execute 'bash ': No such file or directory"

echo "Staring start-wildfly.sh as user $(whoami) with params $@"

# After running each command the content of the "$JBOSS_HOME/standalone/configuration/standalone_xml_history/current" directory
# needs to be deleted as each steps expects it to be empty.  Maybe there is another way??
$JBOSS_HOME/bin/jboss-cli.sh --file=$JBOSS_HOME/bin/keycloak-adapter-configuration.cli
rm -rf $JBOSS_HOME/standalone/configuration/standalone_xml_history/current/*

#From https://hub.docker.com/r/jboss/wildfly
#and https://unix.stackexchange.com/questions/444946/how-can-we-run-a-command-stored-in-a-variable
wildfly_runner=( /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 )
if [ -n "$WILDFLY_MANAGEMENT_USER" ]; then
    /opt/jboss/wildfly/bin/add-user.sh "$WILDFLY_MANAGEMENT_USER" "$WILDFLY_MANAGEMENT_PASSWORD" --silent
fi
#NOTE: as the Readiness and Liveness health probes currently are using POD_IP:9990/health, the management port is required regardless of 
#whether an admin user is specified.  Also tested exposing this port in the Kubernetes Service without the user added and access was not 
#possible to the management console
wildfly_runner+=( -bmanagement 0.0.0.0 )

# See http://tldp.org/LDP/abs/html/comparison-ops.html
if [ -n "$WILDFLY_ENABLE_DEBUG" ] && [ "$WILDFLY_ENABLE_DEBUG" = 'Yes' ]; then
    wildfly_runner+=( --debug )
fi

#From https://stackoverflow.com/a/54775864
wildfly_runner+=( -Djboss.tx.node.id="${MY_POD_NAME/$KUBERNETES_SERVICE_NAME/}" )

if [ -n "$JAVAX_NET_DEBUG" ] && [ "$JAVAX_NET_DEBUG" != 'none' ]; then
    wildfly_runner+=( -Djavax.net.debug=$JAVAX_NET_DEBUG )
fi

if [ -n "$JVM_MAX_HEAP_SIZE" ]; then
    sed -i "s+Xms64m -Xmx512m+Xms$JVM_MAX_HEAP_SIZE -Xmx$JVM_MAX_HEAP_SIZE+g" /opt/jboss/wildfly/bin/standalone.conf
    sed -i "s+MaxMetaspaceSize=256m+MaxMetaspaceSize=$JVM_MAX_HEAP_SIZE+g" /opt/jboss/wildfly/bin/standalone.conf
fi
#    sed -i "s+max-threads count=\"10\"+max-threads count=\"100\"+g" "$JBOSS_HOME/standalone/configuration/standalone.xml"

# JGroups clustering
# JChannel XML configuration file
if [ -n "$JGROUPS_CONFIG" ] && [ "$JGROUPS_CONFIG" != 'none' ]; then
    wildfly_runner+=( -DclusterConfig=$JGROUPS_CONFIG )
fi

# JChannel Name
if [ -n "$JGROUPS_CHANNEL" ] && [ "$JGROUPS_CHANNEL" != 'none' ]; then
    wildfly_runner+=( -DchannelName=$JGROUPS_CHANNEL )
fi

# Change the level of the <console-handler name="CONSOLE"> to the lowest possible level of TRACE
# NOTE: sed -i "/INFO/{s//TRACE/;:p;n;bp}" doesn't work with the logged error message of sed: unterminated {
lineNo=$(grep -Hn '<console-handler' "$JBOSS_HOME/standalone/configuration/standalone.xml" | cut -d ':' -f 2)
sed -i "1,$((lineNo + 1))s/INFO/TRACE/" "$JBOSS_HOME/standalone/configuration/standalone.xml"

# Replace all instances of "DEBUG" with "WARN"
sed -i "s/\"DEBUG\"/\"WARN\"/" "$JBOSS_HOME/standalone/configuration/standalone.xml"

if [ -n "$WILDFLY_LOG_LEVEL" ] && [ "$WILDFLY_LOG_LEVEL" != 'INFO' ]; then
    sed -i "s+<level name=\"INFO\"/>+<level name=\"$WILDFLY_LOG_LEVEL\"/>+g" "$JBOSS_HOME/standalone/configuration/standalone.xml"
fi

#    sed -i "s+<logger category=\"sun.rmi\"+<logger category=\"org.jboss.as.server.deployment\"><level name=\"DEBUG\"/></logger><logger category=\"sun.rmi\"+" "$JBOSS_HOME/standalone/configuration/standalone.xml"
#    sed -i "s+<logger category=\"sun.rmi\"+<logger category=\"org.jboss.jandex\"><level name=\"DEBUG\"/></logger><logger category=\"sun.rmi\"+" "$JBOSS_HOME/standalone/configuration/standalone.xml"

# sed -i "s+<logger category=\"sun.rmi\"+<logger category=\"$PEGACORN_LOG_CATEGORY\"><level name=\"$PEGACORN_LOG_LEVEL\"/></logger><logger category=\"sun.rmi\"+" "$JBOSS_HOME/standalone/configuration/standalone.xml"
# sed -i "s+<logger category=\"sun.rmi\"+<logger category=\"$AETHER_LOG_CATEGORY\"><level name=\"$AETHER_LOG_LEVEL\"/></logger><logger category=\"sun.rmi\"+" "$JBOSS_HOME/standalone/configuration/standalone.xml"

#From https://stackoverflow.com/questions/55112904/mutual-tls-on-apache-camel
wildfly_runner+=( -Djavax.net.ssl.keyStore="/var/lib/pegacorn-keystores/keystore.jks" )
wildfly_runner+=( -Djavax.net.ssl.keyStorePassword="${KEY_PASSWORD}" )

#From https://stackoverflow.com/questions/48521776/wildfly-11-use-certificate-to-make-https-requests
wildfly_runner+=( -Djavax.net.ssl.trustStore="/var/lib/pegacorn-keystores/truststore.jks" )
wildfly_runner+=( -Djavax.net.ssl.trustStorePassword="${TRUSTSTORE_PASSWORD}" )

echo " "
echo "Starting wildfly with the following configuration:"
cat "$JBOSS_HOME/standalone/configuration/standalone.xml"

echo " "
echo "-------------------------------------------------------"
echo "Starting wildfly with the command: ${wildfly_runner[@]}"
echo "-------------------------------------------------------"
"${wildfly_runner[@]}"


