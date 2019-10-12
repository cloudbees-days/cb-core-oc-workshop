import jenkins.model.Jenkins
import hudson.model.Describable
import groovy.io.LineColumnReader
import groovy.json.JsonSlurper
import hudson.security.AuthorizationStrategy
import hudson.security.FullControlOnceLoggedInAuthorizationStrategy
import hudson.security.Permission;
import hudson.security.PermissionGroup;
import nectar.plugins.rbac.groups.*;
import nectar.plugins.rbac.importers.AuthorizationStrategyImporter
import nectar.plugins.rbac.strategy.DefaultRoleMatrixAuthorizationConfig
import nectar.plugins.rbac.strategy.RoleMatrixAuthorizationConfig
import nectar.plugins.rbac.strategy.RoleMatrixAuthorizationPlugin
import nectar.plugins.rbac.strategy.RoleMatrixAuthorizationStrategyImpl
import com.cloudbees.opscenter.server.security.SecurityEnforcer;
import com.cloudbees.opscenter.server.sso.SecurityEnforcerImpl
import java.lang.reflect.Field;
import hudson.scm.SCM;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.User;
import hudson.model.View;


import java.util.logging.Logger

String scriptName = "init.init_04_authorization_rbac.groovy"
int version = 1

int markerVersion = 0
Logger logger = Logger.getLogger(scriptName)

File disableScript = new File(Jenkins.getInstance().getRootDir(), ".disable-authorization-script")
if (disableScript.exists()) {
    logger.info("DISABLE authorization script")
    return
}

File markerFile = new File(Jenkins.getInstance().getRootDir(), ".${scriptName}.done")
if (markerFile.exists()) {
    markerVersion = markerFile.text.toInteger()
}
if (markerVersion == version) {
    logger.info("$scriptName has already been executed for version $version, skipping execution");
    return
}

logger.info("Migrating from version $markerVersion to version $version")

Jenkins jenkins = Jenkins.getInstance()

AuthorizationStrategy authorizationStrategy = jenkins.getAuthorizationStrategy()

String authorizationStrategyBefore = authorizationStrategy.getClass().getName()

    String ROLE_ADMINISTER = "administer";
    String ROLE_DEVELOP = "developer";
    String ROLE_BROWSE = "browse";
    PermissionGroup[] DEVELOP_PERMISSION_GROUPS = [Item.PERMISSIONS, SCM.PERMISSIONS, Run.PERMISSIONS, View.PERMISSIONS];

     RoleMatrixAuthorizationPlugin matrixAuthorizationPlugin = RoleMatrixAuthorizationPlugin.getInstance()
     RoleMatrixAuthorizationConfig config = new DefaultRoleMatrixAuthorizationConfig();
     RoleMatrixAuthorizationStrategyImpl roleMatrixAuthorizationStrategy = new RoleMatrixAuthorizationStrategyImpl()
     jenkins.setAuthorizationStrategy(roleMatrixAuthorizationStrategy)

     Map<String, Set<String>> roles = new HashMap<String, Set<String>>();
     for (Permission p : Permission.getAll()) {
         roles.put(p.getId(), new HashSet<String>(Collections.singleton(ROLE_ADMINISTER)));
     }
     roles.get(Jenkins.READ.getId()).add(ROLE_DEVELOP);
     for (PermissionGroup pg : DEVELOP_PERMISSION_GROUPS) {
         for (Permission p : pg.getPermissions()) {
             roles.get(p.getId()).add(ROLE_DEVELOP);
         }
     }
     roles.get(Jenkins.READ.getId()).add(ROLE_BROWSE);
     roles.get(Item.DISCOVER.getId()).add(ROLE_BROWSE);
     roles.get(Item.READ.getId()).add(ROLE_BROWSE);
     config.setRolesByPermissionIdMap(roles);
     config.setFilterableRoles(new HashSet<String>(Arrays.asList(ROLE_BROWSE, ROLE_DEVELOP)));
     List<Group> rootGroups = new ArrayList<Group>();
     Group g = new Group("Administrators");
     List<String> adminMembers = new ArrayList<String>();
     adminMembers.add("admin")
     adminMembers.add("beedemo-ops")
     g.setMembers(adminMembers);
     g.setRoleAssignments(Collections.singletonList(new Group.RoleAssignment(ROLE_ADMINISTER)));
     rootGroups.add(g);
     g = new Group("Developers");
     g.setMembers(Collections.singletonList("beedemo-dev"));
     g.setRoleAssignments(Collections.singletonList(new Group.RoleAssignment(ROLE_DEVELOP)));
     rootGroups.add(g);
     g = new Group("Browsers");
     g.setMembers(Collections.singletonList("authenticated"));
     g.setRoleAssignments(Collections.singletonList(new Group.RoleAssignment(ROLE_BROWSE)));
     rootGroups.add(g);
     config.setGroups(rootGroups);

     matrixAuthorizationPlugin.configuration = config
     matrixAuthorizationPlugin.save()
     logger.info("RBAC Roles and Groups defined")

    // Set Client Master Security to SSO
    SecurityEnforcer ssoSecurity = new SecurityEnforcerImpl(false, false, null)
    SecurityEnforcer.GlobalConfigurationImpl securityEnforcerConfig = Jenkins.getInstance().getExtensionList(Describable.class).get(SecurityEnforcer.GlobalConfigurationImpl.class);
    securityEnforcerConfig.setGlobal(ssoSecurity)