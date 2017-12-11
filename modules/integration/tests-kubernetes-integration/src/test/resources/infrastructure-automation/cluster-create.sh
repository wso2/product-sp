#! /bin/bash

set -e
#----- ## To create infrastructure and inventory file

#-----This file is invokes via init.sh
log "===Infrastructure preperation script - cluster-create.sh Initiated=="
log "Start creating infrastructure with Terraform"
cd $script_path
TF_LOG=DEBUG OS_DEBUG=1 terraform apply


log "===Good! Successfully created openstack instances now==="
##Run Ansible configurations
log "Quick sleep while instances start and SSH is ready to serve"
sleep 30
log "Ansible provisioning started"
export ANSIBLE_HOST_KEY_CHECKING=False
ansible-playbook -i $script_path/kargo/inventory/inventory -u core -b $script_path/kargo/cluster.yml
log "Ansible Playbook Started"