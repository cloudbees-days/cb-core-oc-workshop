import hudson.model.*;
import jenkins.model.*;

import java.util.logging.Logger

Logger logger = Logger.getLogger("init_02_create-eval-job.groovy")

def j = Jenkins.instance

def name = 'kubernetes shared cloud'
logger.info("creating $name job")
def job = j.getItem(name)
if (job != null) {
  logger.info("job $name already existed so deleting")
  job.delete()
}
println "--> creating $name"

def configXml = """
<com.cloudbees.opscenter.clouds.kubernetes.KubernetesConfiguration plugin="operations-center-kubernetes-cloud@2.176.0.1">
  <actions/>
  <description></description>
  <snippets>
    <com.cloudbees.opscenter.clouds.kubernetes.KubernetesCloudConfigurationSnippet>
      <value>
        <string>&lt;org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud plugin=&quot;kubernetes@1.15.9&quot;&gt;
  &lt;name&gt;kubernetes&lt;/name&gt;
  &lt;defaultsProviderTemplate&gt;default-jnlp&lt;/defaultsProviderTemplate&gt;
  &lt;templates&gt;
    &lt;org.csanchez.jenkins.plugins.kubernetes.PodTemplate&gt;
      &lt;inheritFrom&gt;&lt;/inheritFrom&gt;
      &lt;name&gt;default-jnlp&lt;/name&gt;
      &lt;namespace&gt;&lt;/namespace&gt;
      &lt;privileged&gt;false&lt;/privileged&gt;
      &lt;capOnlyOnAlivePods&gt;false&lt;/capOnlyOnAlivePods&gt;
      &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
      &lt;instanceCap&gt;2147483647&lt;/instanceCap&gt;
      &lt;slaveConnectTimeout&gt;100&lt;/slaveConnectTimeout&gt;
      &lt;idleMinutes&gt;0&lt;/idleMinutes&gt;
      &lt;activeDeadlineSeconds&gt;0&lt;/activeDeadlineSeconds&gt;
      &lt;label&gt;default-jnlp&lt;/label&gt;
      &lt;serviceAccount&gt;jenkins&lt;/serviceAccount&gt;
      &lt;nodeSelector&gt;type=agent&lt;/nodeSelector&gt;
      &lt;nodeUsageMode&gt;NORMAL&lt;/nodeUsageMode&gt;
      &lt;customWorkspaceVolumeEnabled&gt;false&lt;/customWorkspaceVolumeEnabled&gt;
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
          &lt;name&gt;jnlp&lt;/name&gt;
          &lt;image&gt;gcr.io/technologists/k8s-jnlp-agent:0.0.5&lt;/image&gt;
          &lt;privileged&gt;false&lt;/privileged&gt;
          &lt;alwaysPullImage&gt;false&lt;/alwaysPullImage&gt;
          &lt;workingDir&gt;/home/jenkins&lt;/workingDir&gt;
          &lt;command&gt;/bin/sh&lt;/command&gt;
          &lt;args&gt;/var/jenkins_config/jenkins-agent&lt;/args&gt;
          &lt;ttyEnabled&gt;true&lt;/ttyEnabled&gt;
          &lt;resourceRequestCpu&gt;500m&lt;/resourceRequestCpu&gt;
          &lt;resourceRequestMemory&gt;500Mi&lt;/resourceRequestMemory&gt;
          &lt;resourceLimitCpu&gt;1&lt;/resourceLimitCpu&gt;
          &lt;resourceLimitMemory&gt;3Gi&lt;/resourceLimitMemory&gt;
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
      &lt;yamls class=&quot;singleton-list&quot;&gt;
        &lt;string&gt;apiVersion: v1
kind: Pod
metadata:
  name: default-jnlp
spec:
  containers:
  - args:
    - /var/jenkins_config/jenkins-agent
    command:
    - /bin/sh
    image: gcr.io/technologists/k8s-jnlp-agent:0.0.5
    imagePullPolicy: IfNotPresent
    name: jnlp
    resources: {}
    tty: true
    securityContext:
      runAsUser: 1000
  securityContext:
    runAsUser: 1000&lt;/string&gt;
      &lt;/yamls&gt;
      &lt;yamlMergeStrategy class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.yaml.Overrides&quot;/&gt;
      &lt;showRawYaml&gt;true&lt;/showRawYaml&gt;
      &lt;podRetention class=&quot;org.csanchez.jenkins.plugins.kubernetes.pod.retention.Default&quot;/&gt;
    &lt;/org.csanchez.jenkins.plugins.kubernetes.PodTemplate&gt;
  &lt;/templates&gt;
  &lt;serverUrl&gt;&lt;/serverUrl&gt;
  &lt;skipTlsVerify&gt;false&lt;/skipTlsVerify&gt;
  &lt;addMasterProxyEnvVars&gt;false&lt;/addMasterProxyEnvVars&gt;
  &lt;capOnlyOnAlivePods&gt;false&lt;/capOnlyOnAlivePods&gt;
  &lt;containerCap&gt;2147483647&lt;/containerCap&gt;
  &lt;retentionTimeout&gt;5&lt;/retentionTimeout&gt;
  &lt;connectTimeout&gt;0&lt;/connectTimeout&gt;
  &lt;readTimeout&gt;0&lt;/readTimeout&gt;
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
def p = j.createProjectFromXML(name, new ByteArrayInputStream(configXml.getBytes("UTF-8")));
