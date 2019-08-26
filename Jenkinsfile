library 'kypseli@master'
def podYaml = libraryResource 'podtemplates/kubectl.yml'
pipeline {
  agent {
    kubernetes {
      label 'workshop-oc-update'
      defaultContainer 'jnlp'
      yaml podYaml
    }
  }
  options { 
    buildDiscarder(logRotator(numToKeepStr: '5'))
    preserveStashes(buildCount: 5)
  }
  stages {
    stage('Update Config') {
      steps {
        container('kubectl') {
          sh('kubectl -n cje apply -f k8s/casc.yml')
          sh('kubectl -n cje apply -f k8s/kaniko.yml')
          sh('kubectl -n cje apply -f k8s/cb-core-psp.yml')
          sh('kubectl -n cje apply -f k8s/cb-oc.yml')
        } 
      }
    }
  }
}