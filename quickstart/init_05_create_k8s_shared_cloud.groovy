import com.cloudbees.hudson.plugins.folder.*;
import com.cloudbees.opscenter.bluesteel.BlueSteelConstants;
import jenkins.model.*;
import java.util.logging.Logger;

Logger logger = Logger.getLogger("k8s-shared-cloud.groovy")

def j = Jenkins.instance

def teamsFolder = j.getItemByFullName(BlueSteelConstants.CJOC_TEAMS_FOLDER_NAME)
if (teamsFolder == null) {
    teamsFolder = j.createProject(Folder.class, BlueSteelConstants.CJOC_TEAMS_FOLDER_NAME);
}

def name = 'kubernetes shared cloud'
logger.info("creating $name job")
def job = teamsFolder.getItem(name)
if (job != null) {
  logger.info("job $name already existed so deleting")
  job.delete()
}
println "--> creating $name"

def configXml = """
<com.cloudbees.opscenter.clouds.kubernetes.KubernetesConfiguration plugin="operations-center-kubernetes-cloud@2.222.0.2">
  <actions/>
  <description></description>
  <snippets>
    <com.cloudbees.opscenter.clouds.kubernetes.KubernetesCloudConfigurationSnippet>
      <value>
        <string>&lt;org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud plugin=&quot;kubernetes@1.24.1&quot;&gt;
  &lt;name&gt;kubernetes&lt;/name&gt;
  &lt;defaultsProviderTemplate&gt;default-jnlp&lt;/defaultsProviderTemplate&gt;
  &lt;templates&gt;
    &lt;org.csanchez.jenkins.plugins.kubernetes.PodTemplate&gt;
      &lt;inheritFrom&gt;&lt;/inheritFrom&gt;
      &lt;name&gt;default-jnlp&lt;/name&gt;
      &lt;namespace&gt;&lt;/namespace&gt;
      &lt;privileged&gt;false&lt;/privileged&gt;
      &lt;runAsUser&gt;1000&lt;/runAsUser&gt;
      &lt;runAsGroup&gt;1000&lt;/runAsGroup&gt;
      &lt;supplementalGroups&gt;&lt;/supplementalGroups&gt;
      &lt;capOnlyOnAlivePods&gt;false&lt;/capOnlyOnAlivePods&gt;
      &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
      &lt;instanceCap&gt;2147483647&lt;/instanceCap&gt;
      &lt;slaveConnectTimeout&gt;100&lt;/slaveConnectTimeout&gt;
      &lt;idleMinutes&gt;40&lt;/idleMinutes&gt;
      &lt;activeDeadlineSeconds&gt;120&lt;/activeDeadlineSeconds&gt;
      &lt;label&gt;default-jnlp&lt;/label&gt;
      &lt;serviceAccount&gt;jenkins&lt;/serviceAccount&gt;
      &lt;nodeSelector&gt;&lt;/nodeSelector&gt;
      &lt;nodeUsageMode&gt;NORMAL&lt;/nodeUsageMode&gt;
      &lt;hostNetwork&gt;false&lt;/hostNetwork&gt;
      &lt;workspaceVolume class=&quot;org.csanchez.jenkins.plugins.kubernetes.volumes.workspace.EmptyDirWorkspaceVolume&quot;&gt;
        &lt;memory&gt;false&lt;/memory&gt;
      &lt;/workspaceVolume&gt;
      &lt;volumes&gt;
        &lt;org.csanchez.jenkins.plugins.kubernetes.volumes.ConfigMapVolume&gt;
          &lt;mountPath&gt;/var/jenkins_config&lt;/mountPath&gt;
          &lt;configMapName&gt;jenkins-agent&lt;/configMapName&gt;
        &lt;/org.csanchez.jenkins.plugins.kubernetes.volumes.ConfigMapVolume&gt;
      &lt;/volumes&gt;
      &lt;containers&gt;
        &lt;org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate&gt;
          &lt;name&gt;utils&lt;/name&gt;
          &lt;image&gt;gcr.io/core-workshop/k8s-jnlp-agent@sha256:28490f8659bcfdae8159286d6c88fdd7365d6928255103e7500f05dd527bdc8f&lt;/image&gt;
          &lt;privileged&gt;false&lt;/privileged&gt;
          &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
          &lt;workingDir&gt;/home/jenkins/agent&lt;/workingDir&gt;
          &lt;command&gt;sleep&lt;/command&gt;
          &lt;args&gt;99d&lt;/args&gt;
          &lt;ttyEnabled&gt;true&lt;/ttyEnabled&gt;
          &lt;resourceRequestCpu&gt;500m&lt;/resourceRequestCpu&gt;
          &lt;resourceRequestMemory&gt;500Mi&lt;/resourceRequestMemory&gt;
          &lt;resourceLimitCpu&gt;&lt;/resourceLimitCpu&gt;
          &lt;resourceLimitMemory&gt;&lt;/resourceLimitMemory&gt;
          &lt;envVars/&gt;
          &lt;ports/&gt;
          &lt;livenessProbe&gt;
            &lt;execArgs&gt;&lt;/execArgs&gt;
            &lt;timeoutSeconds&gt;0&lt;/timeoutSeconds&gt;
            &lt;initialDelaySeconds&gt;0&lt;/initialDelaySeconds&gt;
            &lt;failureThreshold&gt;0&lt;/failureThreshold&gt;
            &lt;periodSeconds&gt;0&lt;/periodSeconds&gt;
            &lt;successThreshold&gt;0&lt;/successThreshold&gt;
          &lt;/livenessProbe&gt;
        &lt;/org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate&gt;
        &lt;org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate&gt;
          &lt;name&gt;jnlp&lt;/name&gt;
          &lt;image&gt;gcr.io/core-workshop/k8s-jnlp-agent@sha256:28490f8659bcfdae8159286d6c88fdd7365d6928255103e7500f05dd527bdc8f&lt;/image&gt;
          &lt;privileged&gt;false&lt;/privileged&gt;
          &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
          &lt;workingDir&gt;/home/jenkins/agent&lt;/workingDir&gt;
          &lt;command&gt;/bin/sh&lt;/command&gt;
          &lt;args&gt;/var/jenkins_config/jenkins-agent&lt;/args&gt;
          &lt;ttyEnabled&gt;true&lt;/ttyEnabled&gt;
          &lt;resourceRequestCpu&gt;&lt;/resourceRequestCpu&gt;
          &lt;resourceRequestMemory&gt;&lt;/resourceRequestMemory&gt;
          &lt;resourceLimitCpu&gt;&lt;/resourceLimitCpu&gt;
          &lt;resourceLimitMemory&gt;&lt;/resourceLimitMemory&gt;
          &lt;envVars/&gt;
          &lt;ports/&gt;
          &lt;livenessProbe&gt;
            &lt;execArgs&gt;&lt;/execArgs&gt;
            &lt;timeoutSeconds&gt;0&lt;/timeoutSeconds&gt;
            &lt;initialDelaySeconds&gt;0&lt;/initialDelaySeconds&gt;
            &lt;failureThreshold&gt;0&lt;/failureThreshold&gt;
            &lt;periodSeconds&gt;0&lt;/periodSeconds&gt;
            &lt;successThreshold&gt;0&lt;/successThreshold&gt;
          &lt;/livenessProbe&gt;
        &lt;/org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate&gt;
      &lt;/containers&gt;
      &lt;envVars/&gt;
      &lt;annotations/&gt;
      &lt;imagePullSecrets/&gt;
      &lt;nodeProperties/&gt;
      &lt;yamlMergeStrategy class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.yaml.Overrides&quot;/&gt;
      &lt;showRawYaml&gt;true&lt;/showRawYaml&gt;
      &lt;podRetention class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.retention.Default&quot;/&gt;
    &lt;/org.csanchez.jenkins.plugins.kubernetes.PodTemplate&gt;
    &lt;org.csanchez.jenkins.plugins.kubernetes.PodTemplate&gt;
      &lt;inheritFrom&gt;&lt;/inheritFrom&gt;
      &lt;name&gt;nodejs&lt;/name&gt;
      &lt;namespace&gt;&lt;/namespace&gt;
      &lt;privileged&gt;false&lt;/privileged&gt;
      &lt;supplementalGroups&gt;&lt;/supplementalGroups&gt;
      &lt;capOnlyOnAlivePods&gt;false&lt;/capOnlyOnAlivePods&gt;
      &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
      &lt;instanceCap&gt;2147483647&lt;/instanceCap&gt;
      &lt;slaveConnectTimeout&gt;100&lt;/slaveConnectTimeout&gt;
      &lt;idleMinutes&gt;0&lt;/idleMinutes&gt;
      &lt;activeDeadlineSeconds&gt;0&lt;/activeDeadlineSeconds&gt;
      &lt;label&gt;nodejs&lt;/label&gt;
      &lt;nodeSelector&gt;&lt;/nodeSelector&gt;
      &lt;nodeUsageMode&gt;EXCLUSIVE&lt;/nodeUsageMode&gt;
      &lt;hostNetwork&gt;false&lt;/hostNetwork&gt;
      &lt;workspaceVolume class=&quot;org.csanchez.jenkins.plugins.kubernetes.volumes.workspace.EmptyDirWorkspaceVolume&quot;&gt;
        &lt;memory&gt;false&lt;/memory&gt;
      &lt;/workspaceVolume&gt;
      &lt;volumes/&gt;
      &lt;containers&gt;
        &lt;org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate&gt;
          &lt;name&gt;nodejs&lt;/name&gt;
          &lt;image&gt;node:12.16.2-alpine3.11&lt;/image&gt;
          &lt;privileged&gt;false&lt;/privileged&gt;
          &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
          &lt;workingDir&gt;/home/jenkins/agent&lt;/workingDir&gt;
          &lt;command&gt;/bin/sh -c&lt;/command&gt;
          &lt;args&gt;cat&lt;/args&gt;
          &lt;ttyEnabled&gt;true&lt;/ttyEnabled&gt;
          &lt;resourceRequestCpu&gt;&lt;/resourceRequestCpu&gt;
          &lt;resourceRequestMemory&gt;&lt;/resourceRequestMemory&gt;
          &lt;resourceLimitCpu&gt;&lt;/resourceLimitCpu&gt;
          &lt;resourceLimitMemory&gt;&lt;/resourceLimitMemory&gt;
          &lt;envVars/&gt;
          &lt;ports/&gt;
          &lt;livenessProbe&gt;
            &lt;execArgs&gt;&lt;/execArgs&gt;
            &lt;timeoutSeconds&gt;0&lt;/timeoutSeconds&gt;
            &lt;initialDelaySeconds&gt;0&lt;/initialDelaySeconds&gt;
            &lt;failureThreshold&gt;0&lt;/failureThreshold&gt;
            &lt;periodSeconds&gt;0&lt;/periodSeconds&gt;
            &lt;successThreshold&gt;0&lt;/successThreshold&gt;
          &lt;/livenessProbe&gt;
        &lt;/org.csanchez.jenkins.plugins.kubernetes.ContainerTemplate&gt;
      &lt;/containers&gt;
      &lt;envVars/&gt;
      &lt;annotations/&gt;
      &lt;imagePullSecrets/&gt;
      &lt;nodeProperties/&gt;
      &lt;yamlMergeStrategy class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.yaml.Overrides&quot;/&gt;
      &lt;showRawYaml&gt;true&lt;/showRawYaml&gt;
      &lt;podRetention class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.retention.Default&quot;/&gt;
    &lt;/org.csanchez.jenkins.plugins.kubernetes.PodTemplate&gt;
  &lt;/templates&gt;
  &lt;serverUrl&gt;&lt;/serverUrl&gt;
  &lt;skipTlsVerify&gt;false&lt;/skipTlsVerify&gt;
  &lt;addMasterProxyEnvVars&gt;false&lt;/addMasterProxyEnvVars&gt;
  &lt;capOnlyOnAlivePods&gt;false&lt;/capOnlyOnAlivePods&gt;
  &lt;directConnection&gt;false&lt;/directConnection&gt;
  &lt;containerCap&gt;1000&lt;/containerCap&gt;
  &lt;retentionTimeout&gt;5&lt;/retentionTimeout&gt;
  &lt;connectTimeout&gt;5&lt;/connectTimeout&gt;
  &lt;readTimeout&gt;15&lt;/readTimeout&gt;
  &lt;podLabels&gt;
    &lt;org.csanchez.jenkins.plugins.kubernetes.PodLabel&gt;
      &lt;key&gt;jenkins&lt;/key&gt;
      &lt;value&gt;slave&lt;/value&gt;
    &lt;/org.csanchez.jenkins.plugins.kubernetes.PodLabel&gt;
  &lt;/podLabels&gt;
  &lt;usageRestricted&gt;false&lt;/usageRestricted&gt;
  &lt;maxRequestsPerHost&gt;32&lt;/maxRequestsPerHost&gt;
  &lt;waitForPodSec&gt;600&lt;/waitForPodSec&gt;
  &lt;podRetention class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.retention.Never&quot;/&gt;
&lt;/org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud&gt;</string>
      </value>
    </com.cloudbees.opscenter.clouds.kubernetes.KubernetesCloudConfigurationSnippet>
  </snippets>
  <properties/>
</com.cloudbees.opscenter.clouds.kubernetes.KubernetesConfiguration>
"""
def p = teamsFolder.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes("UTF-8")));