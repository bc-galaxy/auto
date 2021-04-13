## 使用

### 环境准备
* java环境<br/>
在编译auto环境之前，请确保java环境安装成功。
  使用`javac` `java`检查java环境是否安装成功。
  ```shell
  mlampdeMacBook-Pro:~ root# javac
  用法: javac <options> <source files>
  其中, 可能的选项包括:
  -g                         生成所有调试信息
  -g:none                    不生成任何调试信息
  -g:{lines,vars,source}     只生成某些调试信息
  -nowarn                    不生成任何警告
  ```
  
* maven环境<br/>
在编译auto环境之前，请确保maven环境安装成功。maven环境用于下载jar包。
  使用 `mvn -v`检查maven环境是否安装成功。
   ```shell
   mlampdeMacBook-Pro:~ root# mvn -v
   Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
   Maven home: /Users/mlamp/dev_tools/apache-maven-3.6.3
   Java version: 1.8.0_275, vendor: AdoptOpenJDK, 
   runtime: /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre
   Default locale: zh_CN, platform encoding: UTF-8
   OS name: "mac os x", version: "10.16", arch: "x86_64", family: "mac"
   ```
* docker环境<br/>
在编译auto镜像之前，请确保docker环境安装成功。
  使用 `docker -v`检查docker环境是否安装成功。
  ```shell
  mlampdeMacBook-Pro:~ root# docker -v
  Docker version 20.10.5, build 55c4c88
  ```
### 镜像编译
*由于github无法上传大文件，所以auto的基础环境中，jdk的tar包无法上传。
需要自己[手动下载jdk](http://download.oracle.com/otn-pub/java/jdk/8u131-b11/d54c1d3a095b4ff2b6607d096fa80163/jdk-8u131-linux-x64.tar.gz)
拷贝至目录 `auto/src/main/docker` 下并修改文件名为  `jdk-8u221-linux-x64.tar.gz`
* 使用docker插件<br/>
  1. 使用docker插件编译需要确保docker开启Remote API访问2375端口。<br/>
  如果未开启可以参考以下方式：
  ```shell
  sudo vim /etc/default/docker
  # 加入以下部分代码
  DOCKER_OPTS="-H tcp://0.0.0.0:2375"
  # 重启docker
  sudo systemctl restart docker
  ```
  2. 执行编译命令。<br/>
  
  ```shell
  # 进入项目根目录
  cd auto
  # 执行编译命令
  mvn clean package docker:build -Dmaven.test.skip=true
  ```
  3. 确认镜像编译完成,使用docker命令查看镜像列表。
  ```shell
  # docker查看镜像列表
  docker images
  
  # 镜像列表出现auto的镜像
  REPOSITORY       TAG         IMAGE ID        CREATED         SIZE
  bc/auto      1.0-SNAPSHOT   0b34a55d22cb   24 hours ago      798MB
  ```
  
* 使用Dockerfile<br/>
  1. 进入根目录，进行jar包的编译。
  ```shell
  # 进入项目根目录
  cd auto
  
  # 执行jar打包命令
  mvn clean install -Dmaven.test.skip=true
  ```
  2. 进入Dockerfile所在目录
  ```shell
  # 进入docker file所在的目录
  cd auto/src/main/docker
  
  # 执行镜像编译命令 ${imageName}是镜像名称，${tag}是镜像标签
  # 如: docker build -f Dockerfile -t auto:0.0.1 .
  docker build -f Dockerfile -t ${imageName}:${tag} .
  ```
### 镜像执行