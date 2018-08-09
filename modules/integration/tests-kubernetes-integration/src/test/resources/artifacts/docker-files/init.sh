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
cd /opt/distribution/msf4j
nohup java -jar test-results-service*.jar > /var/log/msf4j.log  2>&1 &
cd /opt/distribution/wso2sp
# HA_NODE environment variable will be set only in ha-pattern
if [ -n "$CONFIG_FILE" ]
then
    if [ "$CONFIG_FILE" = "node-1" ]
    then
        mv -f deployment-ha-node-1.yaml conf/worker/deployment.yaml
        mv -f log4j2.xml conf/worker/log4j2.xml
        echo "Node 1"
    elif [ "$CONFIG_FILE" = "node-2" ]
    then
        mv -f deployment-ha-node-2.yaml conf/worker/deployment.yaml
        mv -f log4j2.xml conf/worker/log4j2.xml
        echo "Node 2"
    elif [ "$CONFIG_FILE" = "mysql" ]
    then
        mv -f deployment-mysql.yaml conf/worker/deployment.yaml
        mv -f log4j2.xml conf/worker/log4j2.xml
        echo "Node 2"
    else
        echo "Wrong Deployment. Exiting Now"
    fi
fi
sh bin/worker.sh

