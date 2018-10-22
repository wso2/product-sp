############################################################
# Dockerfile to build auto container images
# Based on Ubuntu
############################################################
# set to latest Ubuntu LTS
ARG JDK_BASE=oraclejdk8-8-ubuntu
FROM ${JDK_BASE}:latest
MAINTAINER WSO2 Docker Maintainers "dev@wso2.org"

# set user configurations
ARG USER=wso2carbon
ARG USER_ID=802
ARG USER_GROUP=wso2
ARG USER_GROUP_ID=802
ARG USER_HOME=/home/${USER}
# set dependant files directory
ARG FILES=./tmp/files
#java already setup by the base image

# set wso2 product configurations
ARG WSO2_SERVER=wso2sp
ARG WSO2_SERVER_VERSION=4.3.0
ARG WSO2_SERVER_PACK=${WSO2_SERVER}-${WSO2_SERVER_VERSION}
ARG WSO2_SERVER_HOME=${USER_HOME}/${WSO2_SERVER}-${WSO2_SERVER_VERSION}

# install required packages
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends --no-install-suggests \
    zip \
    unzip \
    telnet \
    vim \
    netcat && \
    rm -rf /var/lib/apt/lists/* && \
    echo '[ ! -z "$TERM" -a -r /etc/motd ] && cat /etc/motd' \
    >> /etc/bash.bashrc \
    ; echo "\
    Welcome to WSO2 Docker resources.\n\
    The Docker container contains the WSO2 product with its latest updates, which are under the End User License Agreement (EULA) 2.0.\n\
    \n\
    Read more about EULA 2.0 (https://wso2.com/licenses/wso2-update/2.0).\n"\
    > /etc/motd

# create a user group and a user
RUN groupadd --system -g ${USER_GROUP_ID} ${USER_GROUP} && \
    useradd --system --create-home --home-dir ${USER_HOME} --no-log-init -g ${USER_GROUP_ID} -u ${USER_ID} ${USER}

# copy the jdk and wso2 product distribution zip files to user's home directory
COPY --chown=wso2carbon:wso2 ${FILES}/${WSO2_SERVER_PACK}/ ${USER_HOME}/${WSO2_SERVER_PACK}/

# copy the jdk and wso2 product distribution zip files to user's home directory
COPY --chown=wso2carbon:wso2 ${FILES}/kafka_2.11_0.10.0.0_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/kafka_clients_0.10.0.0_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/metrics_core_2.2.0_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/scala_library_2.11.8_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/scala_parser_combinators_2.11_1.0.4_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/zkclient_0.8_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/zookeeper_3.4.6_1.0.0.jar ${USER_HOME}/${WSO2_SERVER_PACK}/lib/
COPY --chown=wso2carbon:wso2 ${FILES}/jdbc-drivers/* ${USER_HOME}/${WSO2_SERVER_PACK}/lib/


ENV PATH=$JAVA_HOME/bin:$PATH \
    WSO2_SERVER_HOME=${WSO2_SERVER_HOME} \
    WORKING_DIRECTORY=${USER_HOME}

RUN mkdir -p ${USER_HOME}/msf4j
RUN mkdir -p ${USER_HOME}/wso2sp-deployments
ADD --chown=wso2carbon:wso2 tmp/msf4j ${USER_HOME}/msf4j/

COPY --chown=wso2carbon:wso2 deployment-ha-node-1.yaml ${USER_HOME}/wso2sp-deployments
COPY --chown=wso2carbon:wso2 deployment-ha-node-2.yaml ${USER_HOME}/wso2sp-deployments
COPY --chown=wso2carbon:wso2 log4j2.xml ${USER_HOME}/wso2sp-deployments

COPY --chown=wso2carbon:wso2 init.sh ${USER_HOME}/
RUN chmod +x /home/wso2carbon/init.sh
RUN chown wso2carbon:wso2 ${USER_HOME}/msf4j/
RUN chown wso2carbon:wso2 ${USER_HOME}/wso2sp-deployments/

# set the user and work directory
USER ${USER}
WORKDIR ${USER_HOME}

# expose ports
EXPOSE 32016 32020 30306
ENTRYPOINT ["sh","/home/wso2carbon/init.sh"]
