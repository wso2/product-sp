#!/bin/bash

# Docker init script, this is the Entry Point of the Docker image
# ----- These variables are parsed as Environment variables through Kubernetes controller

#echo "sp_home is : " ${sp_home}
#echo "sp_test_repo is : " ${sp_test_repo}
#echo "sp_test_repo_name is : " ${sp_test_repo_name}
#echo "Deployment Pattern is : " ${pattern}


#cat > /etc/profile.d/set_java_home.sh << EOF
#export JAVA_HOME="/usr/local/java/jdk1.8.0_51"
#export PATH="/usr/local/java/jdk1.8.0_51/bin:\$PATH"
#EOF

#cp ${sp_home}/wso2das-4.0.0-SNAPSHOT /home/distribution

cd /opt/distribution
unzip -q product-4.0.0-SNAPSHOT.zip
cd /wso2das-4.0.0-SNAPSHOT/bin
sh /opt/distribution/wso2das-4.0.0-SNAPSHOT/bin/worker.sh
