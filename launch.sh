#!/bin/bash -eu

. /usr/local/bin/cje-support.sh

generate-passwd-file
maybe-talk-to-castle

set-java-options
export JAVA_OPTS="-Dhudson.slaves.NodeProvisioner.initialDelay=0 ${JVM_OPTS:-} ${JAVA_OPTS:-}"

set-jenkins-options

jenkins-cleanup

#deploy-groovy-scripts

exec /usr/share/jenkins/ref/jenkins.sh "$@"