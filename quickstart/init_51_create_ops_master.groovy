package com.cloudbees.opscenter.server.model

import jenkins.*
import jenkins.model.*
import jenkins.security.*
import jenkins.security.apitoken.*
import hudson.*
import hudson.model.*
import com.cloudbees.hudson.plugins.folder.*;
import com.cloudbees.hudson.plugins.folder.properties.*;
import com.cloudbees.hudson.plugins.folder.properties.FolderCredentialsProvider.FolderCredentialsProperty;
import com.cloudbees.plugins.credentials.impl.*;
import com.cloudbees.plugins.credentials.*;
import com.cloudbees.plugins.credentials.domains.*; 
import com.cloudbees.masterprovisioning.kubernetes.KubernetesMasterProvisioning
import com.cloudbees.opscenter.server.model.ManagedMaster
import com.cloudbees.opscenter.server.properties.ConnectedMasterLicenseServerProperty
import com.cloudbees.opscenter.server.model.OperationsCenter

import java.util.logging.Logger


String scriptName = "init_51_create_ops_master.groovy"

Logger logger = Logger.getLogger(scriptName)

String masterName = "ops"

if(OperationsCenter.getInstance().getConnectedMasters().any { it?.getName()==masterName }) {
    logger.info("Master with this name already exists.")
    return
}

def j = Jenkins.instance
def managedMastersFolder = j.createProject(Folder.class, "managed-masters");
managedMastersFolder.displayName = "Managed Masters"
managedMastersFolder.save()

//create beedemo-ops api token
def userName = 'beedemo-ops'
def tokenName = 'cli-username-token'
  
def user = User.get(userName, false)
def apiTokenProperty = user.getProperty(ApiTokenProperty.class)
def result = apiTokenProperty.tokenStore.generateNewToken(tokenName)
user.save()

//create credentials in ops folder for api token
String id = "cli-username-token"
Credentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, "description:"+id, userName, result.plainValue)

AbstractFolder<?> folderAbs = AbstractFolder.class.cast(managedMastersFolder)
FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
property = new FolderCredentialsProperty([c])
folderAbs.addProperty(property)
logger.info(property.getCredentials().toString())

Map props = [
//    allowExternalAgents: false, //boolean
//    clusterEndpointId: "default", //String
//    cpus: 1.0, //Double
      disk: 50, //Integer //
//    domain: "test-custom-domain-1", //String
//    envVars: "", //String
      fsGroup: "1000", //String
//    image: "custom-image-name", //String -- set this up in Operations Center Docker Image configuration
      javaOptions: "-XshowSettings:vm -XX:MaxRAMFraction=1 -XX:+AlwaysPreTouch -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -XX:+ParallelRefProcEnabled -XX:+UseStringDeduplication -Dhudson.slaves.NodeProvisioner.initialDelay=0 -Djenkins.install.runSetupWizard=false ", //String
//    jenkinsOptions:"", //String
//    kubernetesInternalDomain: "cluster.local", //String
//    livenessInitialDelaySeconds: 300, //Integer
//    livenessPeriodSeconds: 10, //Integer
//    livenessTimeoutSeconds: 10, //Integer
      memory: 3060, //Integer
      namespace: "core-demo", //String
//    ratio: 0.7, //Double
      storageClassName: "ssd", //String
//    terminationGracePeriodSeconds: 1200, //Integer
      yaml:"""
---
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: "nginx"
    
---
kind: StatefulSet
spec:
  template:
    metadata:
      annotations:
          cluster-autoscaler.kubernetes.io/safe-to-evict: "false"
    spec:
      containers:
      - name: jenkins
        env:
          # With the help of SECRETS environment variable
          # we point Jenkins Configuration as Code plugin the location of the secrets
          - name: SECRETS
            value: /var/jenkins_home/mm-secrets
          - name: CASC_JENKINS_CONFIG
            value: https://raw.githubusercontent.com/kypseli/demo-mm-jcasc/ops/jcasc.yml
        volumeMounts:
        - name: mm-secrets
          mountPath: /var/jenkins_home/mm-secrets
          readOnly: true
      volumes:
      - name: mm-secrets
        secret:
          secretName: mm-secrets
      nodeSelector:
        type: master
      securityContext:
        runAsUser: 1000
        fsGroup: 1000  
      """
]

def configuration = new KubernetesMasterProvisioning()
props.each { key, value ->
    configuration."$key" = value
}


ManagedMaster master = managedMastersFolder.createProject(ManagedMaster.class, masterName)

logger.info("Set config...")
master.setConfiguration(configuration)
master.properties.replace(new ConnectedMasterLicenseServerProperty(null))
master.displayName = "Ops Team"

logger.info("Save...")
master.save()

logger.info("Run onModified...")
master.onModified()

logger.info("Provision and start...")
master.provisionAndStartAction();
