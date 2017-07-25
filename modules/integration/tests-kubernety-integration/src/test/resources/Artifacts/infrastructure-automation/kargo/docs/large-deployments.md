Large deployments of K8s
========================

For a large scaled deployments, consider the following configuration changes:

* Tune [ansible settings]
  (http://docs.ansible.com/ansible/intro_configuration.html)
  for `forks` and `timeout` vars to fit large numbers of nodes being deployed.

* Override containers' `foo_image_repo` vars to point to intranet registry.

* Override the ``download_run_once: true`` and/or ``download_localhost: true``.
  See download modes for details.

* Adjust the `retry_stagger` global var as appropriate. It should provide sane
  load on a delegate (the first K8s master node) then retrying failed
  push or download operations.

* Tune parameters for DNS related applications (dnsmasq daemon set, kubedns
  replication controller). Those are ``dns_replicas``, ``dns_cpu_limit``,
  ``dns_cpu_requests``, ``dns_memory_limit``, ``dns_memory_requests``.
  Please note that limits must always be greater than or equal to requests.

* Tune CPU/memory limits and requests. Those are located in roles' defaults
  and named like ``foo_memory_limit``, ``foo_memory_requests`` and
  ``foo_cpu_limit``, ``foo_cpu_requests``. Note that 'Mi' memory units for K8s
  will be submitted as 'M', if applied for ``docker run``, and cpu K8s units
  will end up with the 'm' skipped for docker as well. This is required as
  docker does not understand k8s units well.

* Tune ``kubelet_status_update_frequency`` to increase reliability of kubelet.
  ``kube_controller_node_monitor_grace_period``,
  ``kube_controller_node_monitor_period``,
  ``kube_controller_pod_eviction_timeout`` for better Kubernetes reliability.
  Check out [Kubernetes Reliability](kubernetes-reliability.md)

* Add calico-rr nodes if you are deploying with Calico or Canal. Nodes recover
  from host/network interruption much quicker with calico-rr. Note that
  calico-rr role must be on a host without kube-master or kube-node role (but
  etcd role is okay).

* Check out the
  [Inventory](getting-started.md#building-your-own-inventory)
  section of the Getting started guide for tips on creating a large scale
  Ansible inventory.

For example, when deploying 200 nodes, you may want to run ansible with
``--forks=50``, ``--timeout=600`` and define the ``retry_stagger: 60``.
