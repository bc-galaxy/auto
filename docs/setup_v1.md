## 使用

### 环境准备
auto使用k8s部署，所以需要k8s环境且k8s需要共享证书及通道相关的配置文件，所以需要安装NFS；
如果auto数据需要持久化，则需要可以访问的数据库连接；
如果需要对源码进行编译，则需要安装jdk、maven、git。
* 使用`java` `maven` `git` `kubectl`检查是否具备基础编译环境
```shell
java -version
git version
mvn -v
```
* 使用`kubectl` `showmount`检查是否具备基础的运行环境<br/>
 *showmount是查询NFS挂载的主机地址及挂载的目录*
```shell
kubectl version
showmount -e
```