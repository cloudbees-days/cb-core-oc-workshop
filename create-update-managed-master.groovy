//only runs on CJOC


import com.cloudbees.masterprovisioning.kubernetes.KubernetesMasterProvisioning
import com.cloudbees.opscenter.server.casc.BundleStorage
import com.cloudbees.opscenter.server.model.ManagedMaster
import com.cloudbees.opscenter.server.model.OperationsCenter
import com.cloudbees.opscenter.server.properties.ConnectedMasterLicenseServerProperty
import com.cloudbees.hudson.plugins.folder.Folder;
import nectar.plugins.rbac.groups.Group;
import nectar.plugins.rbac.groups.GroupContainerLocator;
import hudson.ExtensionList
import io.fabric8.kubernetes.client.utils.Serialization
import jenkins.model.Jenkins
import org.apache.commons.io.FileUtils

String masterName = "REPLACE_MASTER_NAME" 
String masterDefinitionYaml = """
bundle:
  jcasc:
    jenkins:
      systemMessage: 'Jenkins configured using CloudBees CI CasC'
    unclassified:
      hibernationConfiguration:
        activities:
        - "build"
        - "web"
        enabled: true
        gracePeriod: 7200
  pluginCatalog:
    configurations:
    - description: tier 3 plugins
      includePlugins:
        cloudbees-disk-usage-simple:
          version: "0.10"
        cloudbees-msteams:
          url: "https://storage.googleapis.com/core-workshop-plugins/cloudbees-msteams-0.2.hpi"  
        extended-read-permission:
          version: "3.2"
        prometheus:
          version: 2.0.7
    displayName: CloudBees CI Workshop Plugin Catalog
    name: cbci-workshop-catalog
    type: plugin-catalog
    version: "1"
  plugins:
  - id: cloudbees-disk-usage-simple
  - id: cloudbees-github-reporting
  - id: cloudbees-groovy-view
  - id: cloudbees-monitoring
  - id: cloudbees-msteams
  - id: cloudbees-nodes-plus
  - id: cloudbees-template
  - id: cloudbees-view-creation-filter
  - id: cloudbees-workflow-template
  - id: cloudbees-workflow-ui
  - id: configuration-as-code
  - id: extended-read-permission
  - id: git
  - id: github-branch-source
  - id: ldap
  - id: managed-master-hibernation
  - id: maven-plugin
  - id: operations-center-cloud
  - id: pipeline-model-extensions
  - id: pipeline-stage-view
  - id: prometheus
  - id: wikitext
  - id: workflow-aggregator
  - id: workflow-cps-checkpoint
provisioning:
  cpus: 1.9
  disk: 10
  memory: 3600
  yaml: |
    kind: Service
    metadata:
      annotations:
        prometheus.io/scheme: 'http'
        prometheus.io/path: '/${masterName}/prometheus'
        prometheus.io/port: '8080'
        prometheus.io/scrape: 'true'
    kind: "StatefulSet"
    spec:
      template:
        spec:
          containers:
          - name: "jenkins"
            env:
            - name: "SECRETS"
              value: "/var/jenkins_home/jcasc_secrets"
            volumeMounts:
            - mountPath: "/var/jenkins_home/jcasc_secrets"
              name: "mm-casc-secrets"
          volumes:
          - name: "mm-casc-secrets"
            secret:
              secretName: "mm-casc-secrets"
"""

def yamlMapper = Serialization.yamlMapper()
Map masterDefinition = yamlMapper.readValue(masterDefinitionYaml, Map.class);

println("Create/update of master '${masterName}' beginning.")

//Either update or create the mm with this config
if (OperationsCenter.getInstance().getConnectedMasters().any { it?.getName() == masterName }) {
    updateMM(masterName, masterDefinition)
} else {
    createMM(masterName, masterDefinition)
}
sleep(150)
println("Finished with master '${masterName}'.\n")


//
//
// only function definitions below here
//
//

private void createMM(String masterName, def masterDefinition) {
    println "Master '${masterName}' does not exist yet. Creating it now."

    def configuration = new KubernetesMasterProvisioning()
    masterDefinition.provisioning.each { k, v ->
        configuration["${k}"] = v
    }

  def teamsFolder = Jenkins.instance.getItem('teams')  
  ManagedMaster master = teamsFolder.createProject(ManagedMaster.class, masterName)
    master.setConfiguration(configuration)
    master.properties.replace(new ConnectedMasterLicenseServerProperty(null))
    master.save()
    master.onModified()

    createEntryInSecurityFile(masterName)
    createOrUpdateBundle(masterDefinition.bundle, masterName)
    setBundleSecurity(masterName, true)

    //ok, now we can actually boot this thing up
    println "Ensuring master '${masterName}' starts..."
    def validActionSet = master.getValidActionSet()
    if (validActionSet.contains(ManagedMaster.Action.ACKNOWLEDGE_ERROR)) {
        master.acknowledgeErrorAction()
        sleep(50)
    }

    validActionSet = master.getValidActionSet()
    if (validActionSet.contains(ManagedMaster.Action.START)) {
        master.startAction();
        sleep(50)
    } else if (validActionSet.contains(ManagedMaster.Action.PROVISION_AND_START)) {
        master.provisionAndStartAction();
        sleep(50)
    } else {
        throw "Cannot start the master." as Throwable
    }
    def Jenkins jenkins = Jenkins.getInstance()
    String roleName = "administer"
    String groupName = "Team Administrators";

	  def teamsFolder = jenkins.getItem("teams")
	  def groupItem = teamsFolder.getItem(masterName);
    def container = GroupContainerLocator.locate(groupItem);
    if(!container.getGroups().any{it.name=groupName}) {
      Group group = new Group(groupName);
      group.doAddMember(masterName);
      group.doGrantRole(roleName, 0, Boolean.TRUE);
      container.addGroup(group);
    }
    sleep(500)
}

private void updateMM(String masterName, def masterDefinition) {
    println "Master '${masterName}' already exists. Updating it."

    ManagedMaster managedMaster = OperationsCenter.getInstance().getConnectedMasters().find { it.name == masterName } as ManagedMaster

    def currentConfiguration = managedMaster.configuration
    masterDefinition.provisioning.each { k, v ->
        if (currentConfiguration["${k}"] != v) {
            currentConfiguration["${k}"] = v
            println "Master '${masterName}' had provisioning configuration item '${k}' change. Updating it."
        }
    }

    createOrUpdateBundle(masterDefinition.bundle, masterName)
    setBundleSecurity(masterName, false)

    managedMaster.configuration = currentConfiguration
    managedMaster.save()

    println "Restarting master '${masterName}'."
    def validActionSet = managedMaster.getValidActionSet()
    if (validActionSet.contains(ManagedMaster.Action.ACKNOWLEDGE_ERROR)) {
        managedMaster.acknowledgeErrorAction()
        sleep(50)
    }

    validActionSet = managedMaster.getValidActionSet()
    if (validActionSet.contains(ManagedMaster.Action.RESTART)) {
        managedMaster.restartAction(false);
        sleep(50)
    } else if (validActionSet.contains(ManagedMaster.Action.START)) {
        managedMaster.startAction();
        sleep(50)
    } else if (validActionSet.contains(ManagedMaster.Action.PROVISION_AND_START)) {
        managedMaster.provisionAndStartAction();
        sleep(50)
    } else {
        throw "Cannot (re)start the master." as Throwable
    }
}

private static void setBundleSecurity(String masterName, boolean regenerateBundleToken) {
    sleep(100)
    ExtensionList.lookupSingleton(BundleStorage.class).initialize()
    BundleStorage.AccessControl accessControl = ExtensionList.lookupSingleton(BundleStorage.class).getAccessControl()
    accessControl.updateMasterPath(masterName, "teams/" + masterName)
    if (regenerateBundleToken) {
        accessControl.regenerate(masterName)
    }
}

private static void createOrUpdateBundle(def bundleDefinition, String masterName) {
    String masterBundleDirPath = getMasterBundleDirPath(masterName)
    def masterBundleDirHandle = new File(masterBundleDirPath)

    File jenkinsYamlHandle = new File(masterBundleDirPath + "/jenkins.yaml")
    File pluginsYamlHandle = new File(masterBundleDirPath + "/plugins.yaml")
    File pluginCatalogYamlHandle = new File(masterBundleDirPath + "/plugin-catalog.yaml")
    File bundleYamlHandle = new File(masterBundleDirPath + "/bundle.yaml")

    int bundleVersion = getExistingBundleVersion(bundleYamlHandle) + 1

    if (masterBundleDirHandle.exists()) {
        FileUtils.forceDelete(masterBundleDirHandle)
    }
    FileUtils.forceMkdir(masterBundleDirHandle)

    def yamlMapper = Serialization.yamlMapper()
    def jcascYaml = yamlMapper.writeValueAsString(bundleDefinition.jcasc)?.replace("---", "")?.trim()
    def pluginsYaml = yamlMapper.writeValueAsString([plugins: bundleDefinition.plugins])?.replace("---", "")?.trim()
    def pluginCatalogYaml = yamlMapper.writeValueAsString(bundleDefinition.pluginCatalog)?.replace("---", "")?.trim()
    def bundleYaml = getBundleYamlContents(masterName, bundleVersion)

    if (jcascYaml == "null") { jcascYaml = "" }
    if (pluginsYaml == "null") { pluginsYaml = "" }
    if (pluginCatalogYaml == "null") { pluginCatalogYaml = "" }

    jenkinsYamlHandle.createNewFile()
    jenkinsYamlHandle.text = jcascYaml

    pluginsYamlHandle.createNewFile()
    pluginsYamlHandle.text = pluginsYaml

    pluginCatalogYamlHandle.createNewFile()
    pluginCatalogYamlHandle.text = pluginCatalogYaml

    bundleYamlHandle.createNewFile()
    bundleYamlHandle.text = bundleYaml
}

private static String getMasterBundleDirPath(String masterName) {
    return "/var/jenkins_home/jcasc-bundles-store/${masterName}"
}

private static void createEntryInSecurityFile(String masterName) {
    //create entry in security file; only the first time we create a bundle and never again. Hopefully this goes
    //away in future versions of CB CasC
    // !!NOTE!! The secret specified here is a stub. It is always regenerated to a proper, secure value. See setBundleSecurity()
    String newerEntry = """\n<entry>
      <string>${masterName}</string>
      <com.cloudbees.opscenter.server.casc.BundleStorage_-AccessControlEntry>
        <secret>{aGVyZWJlZHJhZ29ucwo=}</secret>
        <masterPath>${masterName}</masterPath>
      </com.cloudbees.opscenter.server.casc.BundleStorage_-AccessControlEntry>
    </entry>\n"""

    def cascSecFilePath = "/var/jenkins_home/core-casc-security.xml"
    def cascSecFile = new File(cascSecFilePath)
    String cascSecFileContents = cascSecFile.getText('UTF-8')

    if (cascSecFileContents.contains("<entries/>")) {
        cascSecFileContents = cascSecFileContents.replace("<entries/>", "<entries></entries>")
    } else {
        cascSecFileContents = cascSecFileContents.replace("<entries>", "<entries>${newerEntry}")
    }
    cascSecFile.write(cascSecFileContents)
}

private static String getBundleYamlContents(String masterName, int bundleVersion) {
    return """id: '${masterName}'
version: '${bundleVersion}'
apiVersion: '1'
description: 'Bundle for ${masterName}'
plugins:
- 'plugins.yaml'
jcasc:
- 'jenkins.yaml'
catalog:
- 'plugin-catalog.yaml'
"""
}

private static int getExistingBundleVersion(File bundleYamlFileHandle) {
    if(!bundleYamlFileHandle.exists()) {
        return 0
    }
    def versionLine = bundleYamlFileHandle.readLines().find { it.startsWith("version") }
    String version = versionLine.replace("version:", "").replace(" ", "").replace("'", "").replace('"', '')
    return version as int
}
