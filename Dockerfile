FROM ubuntu:14.04
MAINTAINER Mike Aguila <maguila@ac.upc.edu>

RUN apt-get update && \
    apt-get install -y python-software-properties software-properties-common
RUN add-apt-repository ppa:webupd8team/java

RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 boolean true" | debconf-set-selections

RUN apt-get update && \
    apt-get install -y oracle-java8-installer maven curl


# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

RUN mkdir -p /usr/local/sparksee /home/root/sparksee  /home/root/docker

COPY . /home/root/sparksee

RUN chmod u+x /home/root/sparksee/docker/start.sh

EXPOSE 8182 

ENV MVN_USER sparsity

ENV MVN_PWD sparsity123


ENV SPARKSEE_SERVER_HOME /usr/local/sparksee

ENV PATH $PATH:$SPARKSEE_SERVER_HOME/bin


RUN mkdir -p $HOME/.m2
RUN touch $HOME/.m2/settings.xml

RUN echo "<settings><servers>" >> $HOME/.m2/settings.xml
RUN echo "<server><id>adapt03-libs</id><username>" >> $HOME/.m2/settings.xml
RUN echo $MVN_USER >> $HOME/.m2/settings.xml
RUN echo "</username><password>" >> $HOME/.m2/settings.xml
RUN echo $MVN_PWD >> $HOME/.m2/settings.xml
RUN echo "</password></server>" >> $HOME/.m2/settings.xml

RUN echo "<server><id>adapt03</id><username>" >> $HOME/.m2/settings.xml
RUN echo $MVN_USER >> $HOME/.m2/settings.xml
RUN echo "</username><password>" >> $HOME/.m2/settings.xml
RUN echo $MVN_PWD >> $HOME/.m2/settings.xml
RUN echo "</password></server>" >> $HOME/.m2/settings.xml

RUN echo "<server><id>adapt03-coherentpaas-snapshots</id><username>" >> $HOME/.m2/settings.xml
RUN echo $MVN_USER >> $HOME/.m2/settings.xml
RUN echo "</username><password>" >> $HOME/.m2/settings.xml
RUN echo $MVN_PWD >> $HOME/.m2/settings.xml
RUN echo "</password></server>" >> $HOME/.m2/settings.xml


RUN echo "<server><id>adapt03-remote-libs</id><username>" >> $HOME/.m2/settings.xml
RUN echo $MVN_USER >> $HOME/.m2/settings.xml
RUN echo "</username><password>" >> $HOME/.m2/settings.xml
RUN echo $MVN_PWD >> $HOME/.m2/settings.xml
RUN echo "</password></server>" >> $HOME/.m2/settings.xml


RUN echo "<server><id>adapt03-releases-remote</id><username>" >> $HOME/.m2/settings.xml
RUN echo $MVN_USER >> $HOME/.m2/settings.xml
RUN echo "</username><password>" >> $HOME/.m2/settings.xml
RUN echo $MVN_PWD >> $HOME/.m2/settings.xml
RUN echo "</password></server>" >> $HOME/.m2/settings.xml
RUN echo "</servers></settings>" >> $HOME/.m2/settings.xml

RUN cd /home/root/sparksee && mvn clean install -DskipTests

RUN cp -R /home/root/sparksee/target/sparksee-server-0.0.1-SNAPSHOT-standalone/sparksee-server-0.0.1-SNAPSHOT/* /usr/local/sparksee

CMD ["/home/root/sparksee/docker/start.sh"]

