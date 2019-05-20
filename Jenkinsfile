pipeline {
  agent {
    kubernetes {
      label 'workshop-cleanup'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
metadata:
labels:
  component: ci
spec:
  # Use service account that can deploy to all namespaces
  serviceAccountName: cjoc
  containers:
  - name: kubectl
    image: gcr.io/cloud-builders/kubectl
    command:
    - cat
    tty: true
"""
}
  }
  stages {
    stage('Update Config') {
      steps {
        container('kubectl') {
          sh('kubectl -n cje apply -f casc.yml')
          sh('kubectl -n cje apply -f cb-oc.yml')
        } 
      }
    }
  }
}
