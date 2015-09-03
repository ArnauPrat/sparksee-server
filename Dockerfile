FROM ubuntu:14.04
MAINTAINER Mike Aguila <maguila@ac.upc.edu>

RUN apt-get update && \
    apt-get install -y python-software-properties software-properties-common
RUN add-apt-repository ppa:webupd8team/java

RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 boolean true" | debconf-set-selections

RUN apt-get update && \
    apt-get install -y oracle-java8-installer maven


# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN mkdir -p /usr/local/sparksee /home/root/sparksee  /home/root/docker

COPY . /home/root/sparksee

RUN chmod u+x /home/root/sparksee/docker/start.sh

EXPOSE 8182 


ENV MVN_USER mvnuser

ENV MVN_PWD mvnpwd

CMD ["/home/root/sparksee/docker/start.sh"]