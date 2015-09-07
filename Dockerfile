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

ENV SPARKSEE_CLI_HOME /usr/local/sparksee-cli

ENV GREMLIN_SERVER_HOME /usr/local/sparksee/target/gremlin-server-3.0.0-SNAPSHOT-standalone

ENV PATH $PATH:$GREMLIN_SERVER_HOME/bin:$SPARKSEE_CLI_HOME/bin

CMD ["/home/root/sparksee/docker/start.sh"]