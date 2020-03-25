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

import java.util.logging.Logger;


String scriptName = "init_51_create_ops_master.groovy"

Logger logger = Logger.getLogger(scriptName)

def j = Jenkins.instance

//create beedemo-ops api token
def userName = 'admin'
def tokenName = 'cli-username-token'
  
def user = User.get(userName, false)
def apiTokenProperty = user.getProperty(ApiTokenProperty.class)
def result = apiTokenProperty.tokenStore.generateNewToken(tokenName)
user.save()

//create credentials in ops folder for api token
String id = "cli-username-token"
Credentials c = new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, id, "description:"+id, userName, result.plainValue)

//create cli-username-token credential at Teams folder level for workshop setup 
teamsFolder = j.getItem("Teams")
AbstractFolder<?> folderAbs = AbstractFolder.class.cast(teamsFolder)
FolderCredentialsProperty property = folderAbs.getProperties().get(FolderCredentialsProperty.class)
property = new FolderCredentialsProperty([c])
folderAbs.addProperty(property)
