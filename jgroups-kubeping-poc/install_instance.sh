#!/bin/bash
name="jgroups-kubeping-poc$1"
echo "About to deploy instance = $name"
microk8s.helm3 upgrade $name --install --namespace site-a -f install/kubernetes/systemconfig-kubeping-poc.yaml --set subsystemInstant.subsystemName=$name install/kubernetes/helm
