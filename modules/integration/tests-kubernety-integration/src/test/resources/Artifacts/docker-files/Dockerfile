############################################################
# Dockerfile to build auto container images
# Based on Ubuntu
############################################################
FROM dockerhub.private.wso2.com/ubuntu:14.04
MAINTAINER dasintegrationtest

# Install Java.
RUN \
  apt-get update && \
  apt-get install -y software-properties-common && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y curl git man unzip vim wget && \
#  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer
#  apt-get clean

# Define working directory.
#WORKDIR /data

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

#ENV JAVA_HOME /usr/local/java/jdk1.8.0_51

RUN mkdir -p /opt/distribution
ADD tmp/product-4.0.0-SNAPSHOT.zip /opt/distribution

COPY init.sh /usr/local/bin/init.sh
RUN chmod +x /usr/local/bin/init.sh

#RUN source /usr/local/bin/virtualenvwrapper.sh -----instead of this run below
#RUN /bin/bash -c "source /usr/local/bin/virtualenvwrapper.sh"

EXPOSE 32013

# Docker Entry Point
ENTRYPOINT ["/usr/local/bin/init.sh"]