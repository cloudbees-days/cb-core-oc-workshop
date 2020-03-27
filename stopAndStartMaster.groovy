import com.cloudbees.opscenter.server.model.ManagedMaster

def instance = jenkins.model.Jenkins.instanceOrNull.getItemByFullName("teams/beedemo-dev", ManagedMaster.class)
println "${instance.name}"
println " id: ${instance.id}"
println " idName: ${instance.idName}"

instance.stopAction(true)
sleep 1000
instance.provisionAndStartAction()
