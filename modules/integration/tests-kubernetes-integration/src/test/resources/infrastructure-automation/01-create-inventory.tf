resource "null_resource" "ansible-provision" {
  depends_on = ["openstack_compute_instance_v2.k8s-master","openstack_compute_instance_v2.k8s-node"]

  ##Create Masters Inventory
  provisioner "local-exec" {
    command =  "echo \"[kube-master]\n${openstack_compute_instance_v2.k8s-master.name} ansible_ssh_host=${openstack_compute_instance_v2.k8s-master.network.0.fixed_ip_v4}\" > kargo/inventory/inventory"
  }

  ##Create ETCD Inventory
  provisioner "local-exec" {
    command =  "echo \"\n[etcd]\n${openstack_compute_instance_v2.k8s-master.name} ansible_ssh_host=${openstack_compute_instance_v2.k8s-master.network.0.fixed_ip_v4}\" >> kargo/inventory/inventory"
  }

  ##Create Nodes Inventory
  provisioner "local-exec" {
    command =  "echo \"\n[kube-node]\" >> kargo/inventory/inventory"
  }
  provisioner "local-exec" {
    command =  "echo \"${join("\n",formatlist("%s ansible_ssh_host=%s", openstack_compute_instance_v2.k8s-node.*.name, openstack_compute_instance_v2.k8s-node.*.network.0.fixed_ip_v4))}\" >> kargo/inventory/inventory"
  }

  provisioner "local-exec" {
    command =  "echo \"\n[k8s-cluster:children]\nkube-node\nkube-master\" >> kargo/inventory/inventory"
  }

# Exporting the Kubenetes Master URL
provisioner "local-exec" {
    command =  "echo \"KUBERNETES_MASTER=http://${openstack_compute_instance_v2.k8s-master.network.0.fixed_ip_v4}:8080\" > k8s.properties"
  }
}
