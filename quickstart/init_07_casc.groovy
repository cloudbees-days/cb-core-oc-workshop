import io.jenkins.plugins.casc.ConfigurationAsCode;

import java.util.logging.Logger

String scriptName = "init_07_casc.groovy"

Logger logger = Logger.getLogger(scriptName)

logger.info("attempting to load config as code from https://raw.githubusercontent.com/cloudbees-days/cb-core-oc-workshop/master/jcasc.yml")
ConfigurationAsCode.get().configure("https://raw.githubusercontent.com/cloudbees-days/cb-core-oc-workshop/master/jcasc.yml")
