FROM cloudbees/cloudbees-cloud-core-oc:2.222.1.1-alpine

LABEL maintainer "kmadel@cloudbees.com"

#skip setup wizard and disable CLI
ENV JVM_OPTS -Djenkins.CLI.disabled=true -server
ENV TZ="/usr/share/zoneinfo/America/New_York"

#install suggested and additional plugins
ENV JENKINS_UC http://jenkins-updates.cloudbees.com

RUN mkdir -p /usr/share/jenkins/ref/license-activated-or-renewed-after-expiration.groovy.d

#Jenkins system configuration via init groovy scripts - see https://wiki.jenkins-ci.org/display/JENKINS/Configuring+Jenkins+upon+start+up 
COPY ./quickstart/* /usr/share/jenkins/ref/license-activated-or-renewed-after-expiration.groovy.d/

COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY jenkins-support /usr/local/bin/jenkins-support
COPY install-plugins.sh /usr/local/bin/install-plugins.sh
RUN bash /usr/local/bin/install-plugins.sh < /usr/share/jenkins/ref/plugins.txt

COPY jenkins.sh /usr/share/jenkins/ref
COPY launch.sh /usr/share/jenkins/ref

USER root
RUN chmod +x /usr/share/jenkins/ref/launch.sh /usr/share/jenkins/ref/jenkins.sh
USER 1000

ENTRYPOINT ["tini", "--", "/usr/share/jenkins/ref/launch.sh"]
