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
  stages {
    stage('Update Config') {
      steps {
        container('kubectl') {
          sh('kubectl -n cje apply -f casc.yml')
          sh('kubectl -n cje apply -f cb-core-psp.yml')
          sh('kubectl -n cje apply -f cb-oc.yml')
        } 
      }
    }
  }
}
