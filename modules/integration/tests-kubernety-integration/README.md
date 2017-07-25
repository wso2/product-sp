## SP Integration Test Automation
This repository mainly contains SP Integration test framework source and K8s Infrastructure framework source

### Test framework ###
The repository consists of two segments such as src and the tests. src consists of code related to the framework which does the deployment and run the relevant tests against the given infrastructure. The tests consists SP integration tests classes.

To run the test : 
- mvn clean install

 As prerequisite, Infrastructure automation will be executed first and continue the test suites execution by automation

1. Initially the deploy.sh script will be executed and trigger for the infrastructure script to create instances and K8s cluster deployment
2. Docker image creation will be automate each time before test execute
3. DAS distribution pack will be downloaded - latest pack from jenkins or required pack from local by URL parameter; extract it  and copy into the container.
4. Kubernetes pods will be created and start the docker container on K8s; docker entrypoint will be triggered 
5. After the server startup, tests will be executed.

### Infrastructure framework ###
This repository contains infrastructure creation automation resources for a K8S cluster as below
1. Create instances in openstack - using terraform script
2. Deploy k8s cluster - using ansible scripts
3. Expose the kubernetes master URL

(1) You have to have Terrraform and Ansible setup in the client machine
Terraform : [https://www.terraform.io/intro/getting-started/install.html]
Ansible : [http://docs.ansible.com/ansible/intro_installation.html]

(2) To start the infrastructure seperately
1. First source the openrc.sh file taken from Openstack.
2. Change the terraform.tfvars file with appropriate values.
3. Run init.sh to create infrastructure and deploy the K8S cluster.
4. Run cluster-destrop.sh to destroy the cluster.
