FROM teradatalabs/centos6-java8-oracle
MAINTAINER Grzegorz Kokosiński <grzegorz.kokosinksi@teradata.com>

RUN yum install -y tar

RUN curl -SL http://central.maven.org/maven2/com/facebook/presto/presto-server/0.151/presto-server-0.151.tar.gz \
      | tar xz \
      && mv $(find -type d -name 'presto-server*') presto-server

RUN mkdir /presto-server/etc 

COPY etc /presto-server/etc/

CMD /presto-server/bin/launcher run
