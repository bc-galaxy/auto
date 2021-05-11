## 使用

### 环境准备
* java环境<br/>
在编译auto环境之前，请确保java环境安装成功。
  使用`javac` `java`检查java环境是否安装成功。
  ```shell
  MacBook-Pro:~ root# javac
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
   MacBook-Pro:~ root# mvn -v
   Apache Maven 3.6.3 (cecedd343002696d0abb50b32b541b8a6ba2883f)
   Maven home: /Users/dev_tools/apache-maven-3.6.3
   Java version: 1.8.0_275, vendor: AdoptOpenJDK, 
   runtime: /Library/Java/JavaVirtualMachines/adoptopenjdk-8.jdk/Contents/Home/jre
   Default locale: zh_CN, platform encoding: UTF-8
   OS name: "mac os x", version: "10.16", arch: "x86_64", family: "mac"
   ```
* docker环境<br/>
在编译auto镜像之前，请确保docker环境安装成功。
  使用 `docker -v`检查docker环境是否安装成功。
  ```shell
  MacBook-Pro:~ root# docker -v
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
* kubernetes环境<br/>
确定kubernetes环境安装成功，并且有可调度的node资源。
  ```shell
  root@mason:~# kubectl version
  Client Version: version.Info{Major:"1", Minor:"15", 
    GitVersion:"v1.15.0", 
    GitCommit:"e8462b5b5dc2584fdcd18e6bcfe9f1e4d970a529", 
    GitTreeState:"clean", BuildDate:"2019-06-19T16:40:16Z", 
    GoVersion:"go1.12.5", Compiler:"gc", Platform:"linux/amd64"}
  Server Version: version.Info{Major:"1", Minor:"15", 
    GitVersion:"v1.15.0", 
    GitCommit:"e8462b5b5dc2584fdcd18e6bcfe9f1e4d970a529", 
    GitTreeState:"clean", BuildDate:"2019-06-19T16:32:14Z", 
    GoVersion:"go1.12.5", Compiler:"gc", Platform:"linux/amd64"}
  ```

* NFS环境<br/>
  确定NFS环境安装成功，并且已经挂载目录。
  ```shell
  # 执行命令
  root@mason:~# showmount -e
  # 出现以下内容，为NFS挂载成功
  Export list for mason:
  /home/nfs_data *
  ```
  
* 运行auto镜像<br/>
进入项目deploy目录,查看并编辑启动的yaml文件。
  ```shell
  cd auto/deploy
  ```
  查看并编辑以下auto-deployment.yaml文件。
  ```yaml
  # 创建auto的pv存储
  ---
  apiVersion: v1
  kind: PersistentVolume
  metadata:
    # 创建pv的名称
    name: bc-auto-pv
  spec:
    capacity:
      # pv所需要的存储大小
      storage: 2Mi
    accessModes:
      - ReadWriteMany
    nfs:
      # 挂载nfs的地址
      server: "127.0.0.1"
      # 挂载nfs的地址目录
      path: "/home/nfs_data"
  
  # 创建auto运行namespace
  ---
  apiVersion: v1
  kind: Namespace
  metadata:
    name: bc-auto
  
  # 创建auto的pvc存储
  ---
  apiVersion: v1
  kind: PersistentVolumeClaim
  metadata:
    # pvc的名称
    name: bc-auto-pvc
    # 所属的空间
    namespace: bc-auto
  spec:
    accessModes:
      - ReadWriteMany
    storageClassName: ""
    resources:
      requests:
        storage: 2Mi
    # 属于pv的名称，与上面创建的pv对应
    volumeName: bc-auto-pv
  
  # 创建auto应用部分
  ---
  apiVersion: apps/v1
  kind: Deployment
  metadata:
    # 所属k8s的namespace，与上面创建的namespace对应
    namespace: bc-auto
    # 启动auto应用的名称
    name: bc-auto
    labels:
      app: cluster
      role: bc-auto
  spec:
    replicas: 1
    selector:
      matchLabels:
        app: cluster
        role: bc-auto
    template:
      metadata:
        labels:
          app: cluster
          role: bc-auto
      spec:
        dnsPolicy: ClusterFirst
        # auto的绑定host
        hostAliases:
        - ip: 127.0.0.1
          hostnames:
          - "bc.auto.com"
        containers:
        - name: bc-auto
          # 拉取的镜像名称和tag，与自己编译的镜像保持对应
          image: bc/auto:1.0-SNAPSHOT
          workingDir: /data
          ports:
          - containerPort: 60000
          env:
          - name: LANG
            value: "C.UTF-8"
          command:
          - /bin/bash
          - -c
          - java -jar /work/auto/auto-1.0-SNAPSHOT.jar
          volumeMounts:
            - name: storage
              mountPath: "/data/auto"
        volumes:
        - name: storage
          persistentVolumeClaim:
            claimName: bc-auto-pvc
  
  
  # 创建auto的service地址
  ---
  apiVersion: v1
  kind: Service
  metadata:
    namespace: bc-auto
    name: bc-auto
    labels:
      app: cluster
      role: bc-auto
  spec:
    # 根据需要确定是cluster还是nodeport
    type: NodePort
    selector:
      app: cluster
      role: bc-auto
    ports:
      - name: auto
        port: 60000
        protocol: TCP
        targetPort: 60000
        nodePort: 30000
  ```
  使用k8s启动镜像。
  ```shell
  kubectl create -f auto-deployment.yaml
  ```

### 调用auto
  执行接口调用实现网络、通道、组织、节点的创建及加入等。点击[接口文档]()查看接口列表。<br/>
  查看服务的svc地址，选择 `Endpoints` 后面的ip地址和端口。
  ```shell
  # -n 是启动的namespace，与上面yaml文件填的namespace一致
  root@mason:~# kubectl describe svc -n kube-system
  Name:              kube-dns
  Namespace:         kube-system
  Labels:            k8s-app=kube-dns
                     kubernetes.io/cluster-service=true
                     kubernetes.io/name=KubeDNS
  Annotations:       prometheus.io/port: 9153
                     prometheus.io/scrape: true
  Selector:          k8s-app=kube-dns
  Type:              ClusterIP
  IP:                10.96.0.10
  Port:              dns  53/UDP
  TargetPort:        53/UDP
  Endpoints:         10.244.0.26:53,10.244.0.27:53
  Port:              dns-tcp  53/TCP
  TargetPort:        53/TCP
  Endpoints:         10.244.0.26:53,10.244.0.27:53
  Port:              metrics  9153/TCP
  TargetPort:        9153/TCP
  Endpoints:         10.244.0.26:9153,10.244.0.27:9153
  Session Affinity:  None
  Events:            <none>
  ```
获取 `Endpoints` 地址之后进行接口访问。
```shell
#根据Endpoints地址进行接口访问，进行网络等创建。
curl http://${Endpoints}/***/***
```