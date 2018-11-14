#!/bin/bash

# Docker init script, this is the Entry Point of the Docker image
# ----- These variables are parsed as Environment variables through Kubernetes controller

cd /home/wso2carbon/msf4j
nohup java -jar test-results-service*.jar > msf4j.log  2>&1 &
cd /home/wso2carbon/wso2sp*
# HA_NODE environment variable will be set only in ha-pattern
if [ -n "$CONFIG_FILE" ]
then
    if [ "$CONFIG_FILE" = "node-1" ]
    then
        mv -f /home/wso2carbon/wso2sp-deployments/deployment-ha-node-1.yaml conf/worker/deployment.yaml
        mv -f /home/wso2carbon/wso2sp-deployments/log4j2.xml conf/worker/log4j2.xml
        echo "Node 1"
    elif [ "$CONFIG_FILE" = "node-2" ]
    then
        mv -f /home/wso2carbon/wso2sp-deployments/deployment-ha-node-2.yaml conf/worker/deployment.yaml
        mv -f /home/wso2carbon/wso2sp-deployments/log4j2.xml conf/worker/log4j2.xml
        echo "Node 2"
    elif [ "$CONFIG_FILE" = "mysql" ]
    then
        mv -f /home/wso2carbon/wso2sp-deployments/deployment-mysql.yaml conf/worker/deployment.yaml
        mv -f /home/wso2carbon/wso2sp-deployments/log4j2.xml conf/worker/log4j2.xml
        echo "Node 2"
    else
        echo "Wrong Deployment. Exiting Now"
    fi
fi
sh bin/worker.sh

