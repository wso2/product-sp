##Setup needed variables
variable "node-count" {}
variable "internal-ip-pool" {}
variable "floating-ip-pool" {}
variable "image-name" {}
variable "image-flavor" {}
variable "security-groups" {}
variable "key-pair" {}

resource "openstack_compute_instance_v2" "k8s-master" {
  name = "k8s-master"
  image_name = "${var.image-name}"
  flavor_name = "${var.image-flavor}"
  key_pair = "${var.key-pair}"
  security_groups = ["${split(",", var.security-groups)}"]

  network {
    name = "${var.internal-ip-pool}"
  }
}

resource "openstack_compute_instance_v2" "k8s-node" {
  count = "${var.node-count}"
  name = "k8s-node-${count.index}"
  image_name = "${var.image-name}"
  flavor_name = "${var.image-flavor}"
  key_pair = "${var.key-pair}"
  security_groups = ["${split(",", var.security-groups)}"]
  network {
    name = "${var.internal-ip-pool}"
  }
}
