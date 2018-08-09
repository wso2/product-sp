############################################################
# Dockerfile to build auto container images
# Based on Ubuntu
############################################################
#using pre-configured docker imiage : java8_ubuntu
FROM dockerhub.private.wso2.com/java8_ubuntu

#ubuntu:14.04
MAINTAINER dasintegrationtest

# Install necessary softwares: already updated on the above image
#RUN \
#  apt-get install -y software-properties-common && \
#  apt-get update && \
#  add-apt-repository -y ppa:webupd8team/java && \
#  apt-get update && \
#  apt-get install -y curl git man unzip vim wget && \
#  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
#  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
#  apt-get install -y oracle-java8-installer && \
#  rm -rf /var/lib/apt/lists/* && \
#  rm -rf /var/cache/oracle-jdk8-installer
#  apt-get clean

# Define working directory.
#WORKDIR /data

# Define commonly used JAVA_HOME variable
#ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN mkdir -p /opt/distribution/wso2sp
RUN mkdir /opt/distribution/msf4j
ADD tmp/dist/wso2sp-4.0.0* /opt/distribution/wso2sp/
ADD tmp/msf4j /opt/distribution/msf4j/

ADD deployment-ha-node-1.yaml /opt/distribution/wso2sp
ADD deployment-ha-node-2.yaml /opt/distribution/wso2sp
ADD log4j2.xml /opt/distribution/wso2sp

COPY init.sh /usr/local/bin/init.sh
RUN chmod +x /usr/local/bin/init.sh

#RUN source /usr/local/bin/virtualenvwrapper.sh -----instead of this run below
#RUN /bin/bash -c "source /usr/local/bin/virtualenvwrapper.sh"

EXPOSE 32016 32020 30306

# Docker Entry Point
ENTRYPOINT ["/usr/local/bin/init.sh"]