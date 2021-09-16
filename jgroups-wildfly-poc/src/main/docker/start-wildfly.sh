#!/usr/bin/env bash
# NOTE: this file should have Unix (LF) EOL conversion performed on it to avoid: "env: can't execute 'bash ': No such file or directory"

echo "Staring start-wildfly.sh as user $(whoami) with params $@"

#From https://hub.docker.com/r/jboss/wildfly
#and https://unix.stackexchange.com/questions/444946/how-can-we-run-a-command-stored-in-a-variable
wildfly_runner=( /opt/jboss/wildfly/bin/standalone.sh -b 0.0.0.0 -c standalone-full-ha.xml )
wildfly_runner+=( -bmanagement 0.0.0.0 )
echo " "
echo "Starting wildfly with the following configuration:"
cat "$JBOSS_HOME/standalone/configuration/standalone.xml"

echo " "
echo "-------------------------------------------------------"
echo "Starting wildfly with the command: ${wildfly_runner}"
echo "-------------------------------------------------------"
"${wildfly_runner[@]}"