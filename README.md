# cb-core-oc-workshop
K8s configuration, JCasC, etc for the workshop OC.

## Jenkins Config-as-Code (JCasC)
The JCasC configuration for CJOC is managed as a K8s `ConfigMap` - see [k8s/casc.yml](k8s/casc.yml).

## Core v2 Pod Security Policies
Kubernetes Pod Security Policies (PSP) allow controlling security sensitive aspects of the pod specification - and in the case of Core v2 on K8s that means the CJOC pod itself, master pods and agent pods.

For the most part, one very restrictive PSP will be adequate for all `pods` related to a Core v2 install - see [cb-restricted](k8s/cb-core-psp.yml).

But there is one use case where that PSP will not work - using Kaniko to build container images. Kaniko must run as root and therefore requires its own PSP (and `ServiceAccount`, `Role`, and `RoleBinding`) - see the [K8s config for Kaniko](k8s/kaniko.yml).
