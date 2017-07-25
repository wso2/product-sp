Ansible variables
===============


Inventory
-------------
The inventory is composed of 3 groups:

* **kube-node** : list of kubernetes nodes where the pods will run.
* **kube-master** : list of servers where kubernetes master components (apiserver, scheduler, controller) will run.
* **etcd**: list of servers to compose the etcd server. You should have at least 3 servers for failover purpose.

Note: do not modify the children of _k8s-cluster_, like putting
the _etcd_ group into the _k8s-cluster_, unless you are certain
to do that and you have it fully contained in the latter:

```
k8s-cluster ⊂ etcd => kube-node ∩ etcd = etcd
```

When _kube-node_ contains _etcd_, you define your etcd cluster to be as well schedulable for Kubernetes workloads.
If you want it a standalone, make sure those groups do not intersect.
If you want the server to act both as master and node, the server must be defined
on both groups _kube-master_ and _kube-node_. If you want a standalone and
unschedulable master, the server must be defined only in the _kube-master_ and
not _kube-node_.

There are also two special groups:

* **calico-rr**  : explained for [advanced Calico networking cases](calico.md)
* **bastion** : configure a bastion host if your nodes are not directly reachable

Below is a complete inventory example:

```
## Configure 'ip' variable to bind kubernetes services on a
## different ip than the default iface
node1 ansible_ssh_host=95.54.0.12 ip=10.3.0.1
node2 ansible_ssh_host=95.54.0.13 ip=10.3.0.2
node3 ansible_ssh_host=95.54.0.14 ip=10.3.0.3
node4 ansible_ssh_host=95.54.0.15 ip=10.3.0.4
node5 ansible_ssh_host=95.54.0.16 ip=10.3.0.5
node6 ansible_ssh_host=95.54.0.17 ip=10.3.0.6

[kube-master]
node1
node2

[etcd]
node1
node2
node3

[kube-node]
node2
node3
node4
node5
node6

[k8s-cluster:children]
kube-node
kube-master
```

Group vars and overriding variables precedence
----------------------------------------------

The group variables to control main deployment options are located in the directory ``inventory/group_vars``.
Optional variables are located in the `inventory/group_vars/all.yml`.
Mandatory variables that are common for at least one role (or a node group) can be found in the
`inventory/group_vars/k8s-cluster.yml`.
There are also role vars for docker, rkt, kubernetes preinstall and master roles.
According to the [ansible docs](http://docs.ansible.com/ansible/playbooks_variables.html#variable-precedence-where-should-i-put-a-variable),
those cannot be overriden from the group vars. In order to override, one should use
the `-e ` runtime flags (most simple way) or other layers described in the docs.

Kargo uses only a few layers to override things (or expect them to
be overriden for roles):

Layer | Comment
------|--------
**role defaults** | provides best UX to override things for Kargo deployments
inventory vars | Unused
**inventory group_vars** | Expects users to use ``all.yml``,``k8s-cluster.yml`` etc. to override things
inventory host_vars | Unused
playbook group_vars | Unuses
playbook host_vars | Unused
**host facts** | Kargo overrides for internal roles' logic, like state flags
play vars | Unused
play vars_prompt | Unused
play vars_files | Unused
registered vars | Unused
set_facts | Kargo overrides those, for some places
**role and include vars** | Provides bad UX to override things! Use extra vars to enforce
block vars (only for tasks in block) | Kargo overrides for internal roles' logic
task vars (only for the task) | Unused for roles, but only for helper scripts
**extra vars** (always win precedence) | override with ``ansible-playbook -e @foo.yml``

Ansible tags
------------
The following tags are defined in playbooks:

|                 Tag name | Used for
|--------------------------|---------
|                     apps | K8s apps definitions
|                    azure | Cloud-provider Azure
|                  bastion | Setup ssh config for bastion
|             bootstrap-os | Anything related to host OS configuration
|                   calico | Network plugin Calico
|                    canal | Network plugin Canal
|           cloud-provider | Cloud-provider related tasks
|                  dnsmasq | Configuring DNS stack for hosts and K8s apps
|                   docker | Configuring docker for hosts
|                 download | Fetching container images to a delegate host
|                     etcd | Configuring etcd cluster
|         etcd-pre-upgrade | Upgrading etcd cluster
|             etcd-secrets | Configuring etcd certs/keys
|                 etchosts | Configuring /etc/hosts entries for hosts
|                    facts | Gathering facts and misc check results
|                  flannel | Network plugin flannel
|                      gce | Cloud-provider GCP
|                hyperkube | Manipulations with K8s hyperkube image
|          k8s-pre-upgrade | Upgrading K8s cluster
|              k8s-secrets | Configuring K8s certs/keys
|                      kpm | Installing K8s apps definitions with KPM
|           kube-apiserver | Configuring self-hosted kube-apiserver
|  kube-controller-manager | Configuring self-hosted kube-controller-manager
|                  kubectl | Installing kubectl and bash completion
|                  kubelet | Configuring kubelet service
|               kube-proxy | Configuring self-hosted kube-proxy
|           kube-scheduler | Configuring self-hosted kube-scheduler
|                localhost | Special steps for the localhost (ansible runner)
|                   master | Configuring K8s master node role
|               netchecker | Installing netchecker K8s app
|                  network | Configuring networking plugins for K8s
|                    nginx | Configuring LB for kube-apiserver instances
|                     node | Configuring K8s minion (compute) node role
|                openstack | Cloud-provider OpenStack
|               preinstall | Preliminary configuration steps
|               resolvconf | Configuring /etc/resolv.conf for hosts/apps
|                  upgrade | Upgrading, f.e. container images/binaries
|                   upload | Distributing images/binaries across hosts
|                    weave | Network plugin Weave

Note: Use the ``bash scripts/gen_tags.sh`` command to generate a list of all
tags found in the codebase. New tags will be listed with the empty "Used for"
field.

Example commands
----------------
Example command to filter and apply only DNS configuration tasks and skip
everything else related to host OS configuration and downloading images of containers:

```
ansible-playbook -i inventory/inventory.ini cluster.yml  --tags preinstall,dnsmasq,facts --skip-tags=download,bootstrap-os
```
And this play only removes the K8s cluster DNS resolver IP from hosts' /etc/resolv.conf files:
```
ansible-playbook -i inventory/inventory.ini -e dns_server='' cluster.yml --tags resolvconf
```
And this prepares all container images localy (at the ansible runner node) without installing
or upgrading related stuff or trying to upload container to K8s cluster nodes:
```
ansible-playbook -i inventory/inventory.ini cluster.yml \
    -e download_run_once=true -e download_localhost=true \
    --tags download --skip-tags upload,upgrade
```

Note: use `--tags` and `--skip-tags` wise and only if you're 100% sure what you're doing.

Bastion host
--------------
If you prefer to not make your nodes publicly accessible (nodes with private IPs only),
you can use a so called *bastion* host to connect to your nodes. To specify and use a bastion,
simply add a line to your inventory, where you have to replace x.x.x.x with the public IP of the
bastion host.

```
bastion ansible_ssh_host=x.x.x.x
```

For more information about Ansible and bastion hosts, read
[Running Ansible Through an SSH Bastion Host](http://blog.scottlowe.org/2015/12/24/running-ansible-through-ssh-bastion-host/)
