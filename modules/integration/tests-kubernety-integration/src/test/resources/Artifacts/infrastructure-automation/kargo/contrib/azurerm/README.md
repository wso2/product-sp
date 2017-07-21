# Kubernetes on Azure with Azure Resource Group Templates

Provision the base infrastructure for a Kubernetes cluster by using [Azure Resource Group Templates](https://docs.microsoft.com/en-us/azure/azure-resource-manager/resource-group-authoring-templates)

## Status

This will provision the base infrastructure (vnet, vms, nics, ips, ...) needed for Kubernetes in Azure into the specified
Resource Group. It will not install Kubernetes itself, this has to be done in a later step by yourself (using kargo of course).

## Requirements

- [Install azure-cli](https://docs.microsoft.com/en-us/azure/xplat-cli-install)
- [Login with azure-cli](https://docs.microsoft.com/en-us/azure/xplat-cli-connect)
- Dedicated Resource Group created in the Azure Portal or through azure-cli

## Configuration through group_vars/all

You have to modify at least one variable in group_vars/all, which is the **cluster_name** variable. It must be globally
unique due to some restrictions in Azure. Most other variables should be self explanatory if you have some basic Kubernetes
experience.

## Bastion host

You can enable the use of a Bastion Host by changing **use_bastion** in group_vars/all to **true**. The generated
templates will then include an additional bastion VM which can then be used to connect to the masters and nodes. The option
also removes all public IPs from all other VMs. 

## Generating and applying

To generate and apply the templates, call:

```shell
$ ./apply-rg.sh <resource_group_name>
```

If you change something in the configuration (e.g. number of nodes) later, you can call this again and Azure will
take care about creating/modifying whatever is needed.

## Clearing a resource group

If you need to delete all resources from a resource group, simply call:

```shell
$ ./clear-rg.sh <resource_group_name>
```

**WARNING** this really deletes everything from your resource group, including everything that was later created by you!


## Generating an inventory for kargo

After you have applied the templates, you can generate an inventory with this call:

```shell
$ ./generate-inventory.sh <resource_group_name>
```

It will create the file ./inventory which can then be used with kargo, e.g.:

```shell
$ cd kargo-root-dir
$ ansible-playbook -i contrib/azurerm/inventory -u devops --become -e "@inventory/group_vars/all.yml" cluster.yml
```

