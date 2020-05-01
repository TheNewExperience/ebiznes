



FROM ubuntu:19.04
LABEL maintainer="Tomasz Siola"

RUN apt update&& apt install -y \
   unzip \
   vim \
   git \
   npm 6.8\
   wget

RUN apt-get update && \
   apt-get install -y openjdk-8-jdk && \
   apt-get install -y ant && \
   apt-get clean && \
   rm -rf /var/lib/apt/lists/* && \
   rm -rf /var/cache/oracle-jdk8-installer;

RUN wget http://www.scala-lang.org/files/archive/scala-2.12.8.tgz && \
   tar -xzf /scala-2.12.8.tgz -C /usr/local/ && \
   ln -s /usr/local/scala-2.12.8 /usr/local/scala && \
   rm scala-2.12.8.tgz

RUN wget https://dl.bintray.com/sbt/native-packages/sbt/0.13.6/sbt-0.13.6.tgz && \
   tar -xzf sbt-0.13.6.tgz -C /usr/local/ && \
   rm sbt-0.13.6.tgz






EXPOSE 8000
EXPOSE 9000
EXPOSE 5000
EXPOSE 8888

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
ENV SBT /usr/lib/sbt
ENV SCALA /usr/lib/scala



VOLUME [/home/TomaszSiola/projekt]
RUN export JAVA_HOME


