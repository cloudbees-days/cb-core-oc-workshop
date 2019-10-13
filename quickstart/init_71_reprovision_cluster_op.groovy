import hudson.model.*;
import jenkins.model.*;

import java.util.logging.Logger

Logger logger = Logger.getLogger("init_71_reprovision_cluster_op.groovy")

def j = Jenkins.instance

def name = 'reprovision-masters'
logger.info("creating $name job")
def job = j.getItem(name)
if (job != null) {
  logger.info("job $name already existed so deleting")
  job.delete()
}
println "--> creating $name"

def configXml = """
<com.cloudbees.opscenter.server.clusterops.ClusterOpProject plugin="operations-center-clusterops@2.176.0.1">
  <description></description>
  <keepDependencies>false</keepDependencies>
  <properties/>
  <scm class="hudson.scm.NullSCM"/>
  <canRoam>true</canRoam>
  <disabled>false</disabled>
  <blockBuildWhenDownstreamBuilding>false</blockBuildWhenDownstreamBuilding>
  <blockBuildWhenUpstreamBuilding>false</blockBuildWhenUpstreamBuilding>
  <triggers/>
  <concurrentBuild>true</concurrentBuild>
  <builders/>
  <publishers/>
  <buildWrappers/>
  <operations>
    <com.cloudbees.pse.masterprovisioning.clusterops.operations.ManagedMasterClusterOperation plugin="master-provisioning-core@2.2.9">
      <itemSource class="com.cloudbees.opscenter.server.clusterops.sources.JenkinsRootItemSource" plugin="operations-center-clusterops@2.176.0.1"/>
      <filters>
        <com.cloudbees.opscenter.server.clusterops.filter.MatchingRegex plugin="operations-center-clusterops@2.176.0.1">
          <regex>^((?!ops).)*</regex>
        </com.cloudbees.opscenter.server.clusterops.filter.MatchingRegex>
      </filters>
      <clusterOpSteps>
        <com.cloudbees.pse.masterprovisioning.clusterops.steps.ReprovisionClusterOpStep>
          <failOnError>false</failOnError>
          <force>true</force>
        </com.cloudbees.pse.masterprovisioning.clusterops.steps.ReprovisionClusterOpStep>
      </clusterOpSteps>
      <noRetries>0</noRetries>
      <inParallel>0</inParallel>
      <timeoutSeconds>0</timeoutSeconds>
      <failureMode>IMMEDIATELY</failureMode>
      <failAs>
        <name>FAILURE</name>
        <ordinal>2</ordinal>
        <color>RED</color>
        <completeBuild>true</completeBuild>
      </failAs>
    </com.cloudbees.pse.masterprovisioning.clusterops.operations.ManagedMasterClusterOperation>
  </operations>
</com.cloudbees.opscenter.server.clusterops.ClusterOpProject>
"""

def configJobsFolder = j.createProject(Folder.class, "config-jobs");
def p = configJobsFolder.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes("UTF-8")));