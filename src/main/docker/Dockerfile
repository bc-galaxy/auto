FROM ubuntu:16.04
MAINTAINER Mason
WORKDIR /data/auto
# 更新apt
ADD sources.list /etc/apt/sources.list
RUN apt-get -y update
RUN apt-get install -y libltdl7

# 添加并设置默认时区
ADD zoneinfo /usr/share/zoneinfo
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime


RUN mkdir -p /data/share && mkdir -p /data/auto && mkdir -p /bc/auto && mkdir -p /data/auto/generate && mkdir -p /data/auto/certs

# 添加kubectl连接配置文件及证书秘钥
ADD auto /data/share

# 安装配置java环境
RUN mkdir /usr/local/java
ADD jdk-8u221-linux-x64.tar.gz /usr/local/java/
ENV JAVA_HOME=/usr/local/java/jdk1.8.0_221
ENV JRE_HOME $JAVA_HOME/jre
ENV CLASSPATH $JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar:$JRE_HOME/lib:$CLASSPATH
ENV PATH $JAVA_HOME/bin:$PATH

ADD auto-1.0-SNAPSHOT.jar /bc/auto/auto-1.0-SNAPSHOT.jar