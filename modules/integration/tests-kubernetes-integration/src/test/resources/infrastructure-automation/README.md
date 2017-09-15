# infrastructure-automation
This repository contains infrastructure creation automation resources for a K8S cluster. Terraform scripts for Openstack and Ansible scripts for K8S.

Note : make sure you have Terrraform and Ansible setup in the client machine

* First source the openrc.sh file taken from Openstack.
* Change the terraform.tfvars file with appropriate values.
* Run init.sh to create infrastructure and deploy the K8S cluster.
* Run cluster-destrop.sh to destroy the cluster.
