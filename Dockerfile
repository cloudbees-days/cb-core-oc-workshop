FROM cloudbees/cloudbees-cloud-core-oc:2.176.2.3

LABEL maintainer "kmadel@cloudbees.com"

#skip setup wizard and disable CLI
ENV JVM_OPTS -Djenkins.CLI.disabled=true -server
ENV TZ="/usr/share/zoneinfo/America/New_York"

#install suggested and additional plugins
ENV JENKINS_UC http://jenkins-updates.cloudbees.com

RUN mkdir -p /usr/share/jenkins/ref/plugins

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY jenkins-support /usr/local/bin/jenkins-support
COPY install-plugins.sh /usr/local/bin/install-plugins.sh
RUN bash /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt