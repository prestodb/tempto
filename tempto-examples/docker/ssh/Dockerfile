FROM prestodb/centos6-oj8
MAINTAINER Presto community <https://prestodb.io/community.html>

RUN yum install -y openssh-server openssh-clients passwd
RUN echo s3cr37_p@55 | passwd --stdin root

RUN mkdir /var/run/sshd && mkdir /root/.ssh

RUN ssh-keygen -t rsa -f /etc/ssh/ssh_host_rsa_key -N '' 

COPY keys/key.pub /root/.ssh/authorized_keys

RUN chmod 400 /root/.ssh/authorized_keys

EXPOSE 22

CMD /usr/sbin/sshd -D
