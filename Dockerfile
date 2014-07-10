FROM aslag/centos6_jdk8_5:vanilla
MAINTAINER mdye <michael.dye@sungard.com>

# see https://bugs.centos.org/view.php?id=7126
RUN sed -i '/^\[centosplus\]$/,/^\[/ s/^enabled=0$/enabled=1/' /etc/yum.repos.d/CentOS-Base.repo

# from http://docs.docker.io/en/latest/examples/nodejs_web_app/
RUN rpm -Uvh http://download.fedoraproject.org/pub/epel/6/i386/epel-release-6-8.noarch.rpm
RUN yum install -y make tar bzip2 git unzip lsof tree

RUN cd / && git clone https://bitbucket.org/mdye/docker-container_setup.git
RUN /docker-container_setup/centos/ssh_setup.bash
RUN /docker-container_setup/centos/supervisor_setup.bash
RUN /docker-container_setup/centos/vim_setup.bash
RUN /docker-container_setup/centos/wrk_setup.bash
RUN /docker-container_setup/centos/rsyncd_backdoor_setup.bash

RUN echo 'root:devo' | chpasswd

# fetch, install leiningen
RUN curl -s https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o /usr/local/bin/lein && chmod +x $_ && echo 'export LEIN_ROOT=yes' >> /root/.profile && . /root/.profile && lein

# setup project working dir
RUN mkdir /work

# N.B. need to run this manually in containers that have been committed after usind "docker run" with other commands
CMD ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisord.conf"]
