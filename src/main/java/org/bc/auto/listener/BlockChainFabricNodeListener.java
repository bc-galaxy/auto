package org.bc.auto.listener;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import org.bc.auto.config.BlockChainFabricConstructConstant;
import org.bc.auto.config.BlockChainFabricImagesConstant;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.listener.source.BlockChainFabricNodeEventSource;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.model.vo.FabricConstructVo;
import org.bc.auto.service.ClusterService;
import org.bc.auto.service.NodeService;
import org.bc.auto.utils.K8SUtils;
import org.bc.auto.utils.SpringBeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BlockChainFabricNodeListener implements BlockChainListener{
    private static final Logger logger = LoggerFactory.getLogger(BlockChainFabricOrgListener.class);

    @Override
    public void doEven(BlockChainEvent blockChainEven) {
        ClusterService clusterService = SpringBeanUtil.getBean(ClusterService.class);
        NodeService nodeService = SpringBeanUtil.getBean(NodeService.class);

        //开始启动K8S的节点(分为Orderer节点和普通的Peer节点)
        //获取事件的参数实体对象
        BlockChainFabricNodeEventSource<BCNode> bcNodeBlockChainArrayList = (BlockChainFabricNodeEventSource<BCNode>)blockChainEven.getBlockChainEventSource();
        List<BCNode> nodeList = bcNodeBlockChainArrayList.geteList();

        for (BCNode bcNode:nodeList) {
            //获取集群对象
            BCCluster bcCluster = clusterService.getBCCluster(bcNode.getClusterId());
            //使用VO对象存储变量
            FabricConstructVo fabricConstructVo = new FabricConstructVo();
            fabricConstructVo.setNodeName(bcNode.getNodeName());
            fabricConstructVo.setNodeDomain(bcNode.getNodeName()+ "." +bcCluster.getClusterName());
            fabricConstructVo.setStateDbType(bcCluster.getStateDbType());
            fabricConstructVo.setClusterVersion(bcCluster.getClusterVersion());
            if(bcNode.getNodeType().intValue() == 1){
                fabricConstructVo.setCertPath(bcCluster.getClusterName() + "/crypto-config/ordererOrganizations/" + bcCluster.getClusterName() + "/orderers/");
                fabricConstructVo.setDataPath(bcCluster.getClusterName() + "/data/" + bcNode.getOrgName().toLowerCase() + "-");
                fabricConstructVo.setMonitorPort(8443);
                fabricConstructVo.setNameSapce(bcCluster.getClusterName());
                fabricConstructVo.setNodeK8sRole("orderer");
                fabricConstructVo.setNodePort(7050);
                fabricConstructVo.setOrgName(bcNode.getOrgName());
                fabricConstructVo.setOrgMspId(String.join("",bcNode.getOrgName(),"MSP"));
                fabricConstructVo.setTlsEnable("true");
                fabricConstructVo.setImageName(BlockChainFabricImagesConstant.getFabricOrdereImage(bcCluster.getClusterVersion()));
                bcNode = startOrderer(fabricConstructVo,bcNode);
            }else{
                fabricConstructVo.setCertPath(bcCluster.getClusterName() + "/crypto-config/peerOrganizations/" + bcNode.getOrgName() + "-" + bcCluster.getClusterName() + "/peers/" + fabricConstructVo.getNodeDomain());
                fabricConstructVo.setDataPath(bcCluster.getClusterName() + "/data/" + bcNode.getOrgName().toLowerCase() + "-" + bcNode.getNodeName());
                fabricConstructVo.setMonitorPort(9443);
                fabricConstructVo.setNameSapce(bcCluster.getClusterName());
                fabricConstructVo.setNodeK8sRole("peer");
                fabricConstructVo.setNodePort(7051);
                fabricConstructVo.setOrgName(bcNode.getOrgName());
                fabricConstructVo.setOrgMspId(String.join("",bcNode.getOrgName(),"MSP"));
                fabricConstructVo.setTlsEnable("true");
                fabricConstructVo.setImageName(BlockChainFabricImagesConstant.getFabricPeerImage(bcCluster.getClusterVersion()));
                fabricConstructVo.setChainCodePort(7052);
                bcNode = startPeer(fabricConstructVo,bcNode);
            }
            nodeService.updateNode(bcNode);
        }
    }

    //启动Orderer的pod
    private BCNode startOrderer(FabricConstructVo fabricConstructVo,BCNode bcNode){
        //创建pod的deployment
        final String logLevel = "INFO";
        final String tlsEnable = "true";
        // 查询节点ip
//        String nodeIp = K8SUtils.queryNodeIp();

        final Map<String, String> labels = new HashMap<String, String>(4) {{
            put("namespace", fabricConstructVo.getNameSapce());
            put("app", "auto-cluster");
            put("role", fabricConstructVo.getNodeK8sRole());
            put("orderer-name", fabricConstructVo.getNodeName());
        }};

        V1PersistentVolumeClaimVolumeSource v1PersistentVolumeClaimVolumeSource = new V1PersistentVolumeClaimVolumeSource().claimName(BlockChainK8SConstant.getK8sPvcName(fabricConstructVo.getNameSapce())).readOnly(false);
        // 创建deployment
        V1PodSpec podSpec = new V1PodSpec()
//                .hostAliases(new ArrayList<V1HostAlias>() {{
//                    add(new V1HostAlias().ip("127.0.0.1").hostnames(Arrays.asList(fabricConstructVo.getNodeDomain())));
//                }})
                .containers(new ArrayList<V1Container>() {{
                    add(new V1Container()
                            .name(fabricConstructVo.getNodeName())
                            .image(fabricConstructVo.getImageName())
                            .command(new ArrayList<String>() {{
                                add("/bin/bash");
                                add("-c");
                                add("orderer");
                                // add("orderer &>> /opt/log/logger.txt");
                            }})
                            .workingDir("/opt/gopath/src/github.com/hyperledger/fabric")
                            .env(new ArrayList<V1EnvVar>() {{
                                add(new V1EnvVar().name("TZ").value("Asia/Shanghai"));
                                add(new V1EnvVar().name("FABRIC_LOGGING_SPEC").value(logLevel));
                                add(new V1EnvVar().name("ORDERER_GENERAL_LISTENADDRESS").value("0.0.0.0"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_GENESISMETHOD").value("file"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_GENESISFILE").value("/var/hyperledger/orderer/genesis.block"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_LOCALMSPID").value(fabricConstructVo.getOrgMspId()));
                                add(new V1EnvVar().name("ORDERER_GENERAL_LOCALMSPDIR").value("/var/hyperledger/orderer/msp"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_TLS_ENABLED").value(fabricConstructVo.getTlsEnable()));
                                add(new V1EnvVar().name("ORDERER_GENERAL_TLS_PRIVATEKEY").value("/var/hyperledger/orderer/tls/server.key"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_TLS_CERTIFICATE").value("/var/hyperledger/orderer/tls/server.crt"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_TLS_ROOTCAS").value("[/var/hyperledger/orderer/tls/ca.crt]"));
                                add(new V1EnvVar().name("ORDERER_KAFKA_TOPIC_REPLICATIONFACTOR").value("1"));
                                add(new V1EnvVar().name("ORDERER_KAFKA_VERBOSE").value("true"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_CLUSTER_CLIENTCERTIFICATE").value("/var/hyperledger/orderer/tls/server.crt"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_CLUSTER_CLIENTPRIVATEKEY").value("/var/hyperledger/orderer/tls/server.key"));
                                add(new V1EnvVar().name("ORDERER_GENERAL_CLUSTER_ROOTCAS").value("[/var/hyperledger/orderer/tls/ca.crt]"));
                                add(new V1EnvVar().name("CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE").value("bridge"));
                                add(new V1EnvVar().name("ORDERER_METRICS_PROVIDER").value("prometheus"));
                                add(new V1EnvVar().name("ORDERER_OPERATIONS_LISTENADDRESS").value("0.0.0.0:8443"));
                            }})
                            .ports(new ArrayList<V1ContainerPort>() {{
                                add(new V1ContainerPort().containerPort(fabricConstructVo.getNodePort()));
                                add(new V1ContainerPort().containerPort(fabricConstructVo.getMonitorPort()));
                            }})
                            .volumeMounts(new ArrayList<V1VolumeMount>() {{
                                add(new V1VolumeMount().name("orderer-data").mountPath("/var/hyperledger/orderer/genesis.block").subPath(fabricConstructVo.getNameSapce() + "/channels/genesis.block"));
                                add(new V1VolumeMount().name("orderer-data").mountPath("/var/hyperledger/orderer/msp").subPath(fabricConstructVo.getCertPath() + fabricConstructVo.getNodeDomain() + "/msp"));
                                add(new V1VolumeMount().name("orderer-data").mountPath("/var/hyperledger/orderer/tls").subPath(fabricConstructVo.getCertPath() + fabricConstructVo.getNodeDomain() + "/tls"));
                                add(new V1VolumeMount().name("orderer-data").mountPath("/etc/hyperledger/fabric/orderer.yaml").subPath(fabricConstructVo.getNameSapce() + "/config/orderer.yaml"));
                                add(new V1VolumeMount().name("orderer-data").mountPath("/etc/hyperledger/fabric/core.yaml").subPath(fabricConstructVo.getNameSapce() + "/config/core.yaml"));
                                add(new V1VolumeMount().name("orderer-data").mountPath("/var/hyperledger/production").subPath(fabricConstructVo.getDataPath() + fabricConstructVo.getNodeName() + "/production"));
                                add(new V1VolumeMount().name("orderer-data").mountPath("/opt/log").subPath(fabricConstructVo.getDataPath() + fabricConstructVo.getNodeName() + "/log"));
                            }})
                    );
                }})
                .volumes(new ArrayList<V1Volume>() {{
                    add(new V1Volume()
                            .name("orderer-data")
                            .persistentVolumeClaim(v1PersistentVolumeClaimVolumeSource)
                    );
                }});
        K8SUtils.createDeployment(fabricConstructVo.getNameSapce(), fabricConstructVo.getNodeName(), labels, podSpec);

        String nodeTlsCaPath = fabricConstructVo.getCertPath() + "/"+fabricConstructVo.getNameSapce() + "/crypto-config/ordererOrganizations/" + fabricConstructVo.getNameSapce() + "/orderers/"+fabricConstructVo.getNodeDomain() + "/tls/ca.crt";
        bcNode.setNodePort(fabricConstructVo.getNodePort());
        bcNode.setNodeIp(fabricConstructVo.getNodeDomain());
        bcNode.setNodeTlsPath(nodeTlsCaPath);

        return bcNode;
    }

    private BCNode startPeer(FabricConstructVo fabricConstructVo,BCNode bcNode){
        final String logLevel = "INFO";
        final String tlsEnable = "true";
        final String peerCertPath = fabricConstructVo.getNameSapce() + "/crypto-config/peerOrganizations/" + fabricConstructVo.getOrgName().toLowerCase() + "-" + fabricConstructVo.getNameSapce() + "/peers/" + fabricConstructVo.getNodeDomain();
        final String peerDataPath = fabricConstructVo.getNameSapce() + "/data/" + fabricConstructVo.getOrgName().toLowerCase() + "-" + fabricConstructVo.getNodeName();

        final Map<String, String> labels = new HashMap<String, String>(4) {{
            put("namespace", fabricConstructVo.getNameSapce());
            put("app", fabricConstructVo.getNodeName());
            put("role", fabricConstructVo.getNodeK8sRole());
            put("peer-name", fabricConstructVo.getNodeName());
        }};

        // 创建service
        V1ServiceSpec svcSpec = new V1ServiceSpec()
                .type("NodePort")
                .selector(labels)
                .ports(new ArrayList<V1ServicePort>() {{
                    add(new V1ServicePort().name("listen").port(fabricConstructVo.getNodePort()).targetPort(new IntOrString(fabricConstructVo.getNodePort())));
                    add(new V1ServicePort().name("chaincode").port(fabricConstructVo.getChainCodePort()).targetPort(new IntOrString(fabricConstructVo.getChainCodePort())));
                    add(new V1ServicePort().name("monitor").port(fabricConstructVo.getMonitorPort()).targetPort(new IntOrString(fabricConstructVo.getMonitorPort())));
                }});
        K8SUtils.createService(fabricConstructVo.getNameSapce(), fabricConstructVo.getNodeName(), labels, svcSpec);

        // 查询节点ip
//        String nodeIp = K8SUtils.queryNodeIp();

        // 获取peer服务的cluster-ip
        String clusterIp = K8SUtils.queryClusterIp(fabricConstructVo.getNodeName(), fabricConstructVo.getNameSapce());

        // peer container常用的环境变量设置
        ArrayList<V1EnvVar> v1EnvVars = new ArrayList<V1EnvVar>() {{
            add(new V1EnvVar().name("TZ").value("Asia/Shanghai"));
            add(new V1EnvVar().name("CORE_PEER_ID").value(fabricConstructVo.getNodeDomain()));
            add(new V1EnvVar().name("CORE_PEER_ADDRESS").value(fabricConstructVo.getNodeDomain() + ":" + fabricConstructVo.getNodePort()));
            add(new V1EnvVar().name("CORE_PEER_LISTENADDRESS").value("0.0.0.0:" + fabricConstructVo.getNodePort()));
            add(new V1EnvVar().name("CORE_PEER_CHAINCODEADDRESS").value(clusterIp + ":" + fabricConstructVo.getChainCodePort()));
            add(new V1EnvVar().name("CORE_PEER_CHAINCODELISTENADDRESS").value("0.0.0.0:" + fabricConstructVo.getChainCodePort()));
            add(new V1EnvVar().name("CORE_PEER_GOSSIP_BOOTSTRAP").value(fabricConstructVo.getNodeDomain() + ":" + fabricConstructVo.getNodePort()));
            add(new V1EnvVar().name("CORE_PEER_GOSSIP_EXTERNALENDPOINT").value(fabricConstructVo.getNodeDomain() + ":" + fabricConstructVo.getNodePort()));
            add(new V1EnvVar().name("CORE_METRICS_PROVIDER").value("prometheus"));
            add(new V1EnvVar().name("CORE_OPERATIONS_LISTENADDRESS").value("0.0.0.0:"+fabricConstructVo.getMonitorPort()));
            add(new V1EnvVar().name("CORE_PEER_LOCALMSPID").value(fabricConstructVo.getOrgMspId()));
            add(new V1EnvVar().name("CORE_VM_ENDPOINT").value("unix:///host/var/run/docker.sock"));
            add(new V1EnvVar().name("CORE_VM_DOCKER_HOSTCONFIG_NETWORKMODE").value("bridge"));
            add(new V1EnvVar().name("FABRIC_LOGGING_SPEC").value(logLevel));
            add(new V1EnvVar().name("CORE_PEER_TLS_ENABLED").value(tlsEnable));
            add(new V1EnvVar().name("CORE_PEER_GOSSIP_USELEADERELECTION").value("true"));
            add(new V1EnvVar().name("CORE_PEER_GOSSIP_ORGLEADER").value("false"));
            add(new V1EnvVar().name("CORE_PEER_PROFILE_ENABLED").value("true"));
            add(new V1EnvVar().name("CORE_PEER_TLS_CERT_FILE").value("/etc/hyperledger/fabric/tls/server.crt"));
            add(new V1EnvVar().name("CORE_PEER_TLS_KEY_FILE").value("/etc/hyperledger/fabric/tls/server.key"));
            add(new V1EnvVar().name("CORE_PEER_TLS_ROOTCERT_FILE").value("/etc/hyperledger/fabric/tls/ca.crt"));
        }};

        ArrayList<V1Container> v1Containers = new ArrayList<>();

        if (fabricConstructVo.getStateDbType() == 1) {
            // 如果选择 couch db做环境变量，需要在peer container里面添加以下四个环境变量，同时新增一个peer-couchdb 的container
            v1EnvVars.add(new V1EnvVar().name("CORE_LEDGER_STATE_STATEDATABASE").value("CouchDB"));
            v1EnvVars.add(new V1EnvVar().name("CORE_LEDGER_STATE_COUCHDBCONFIG_COUCHDBADDRESS").value("localhost:" + BlockChainFabricConstructConstant.COUCH_DB_PORT));
            v1EnvVars.add(new V1EnvVar().name("CORE_LEDGER_STATE_COUCHDBCONFIG_USERNAME").value(BlockChainFabricConstructConstant.COUCH_DB_USERNAME));
            v1EnvVars.add(new V1EnvVar().name("CORE_LEDGER_STATE_COUCHDBCONFIG_PASSWORD").value(BlockChainFabricConstructConstant.COUCH_DB_PASSWORD));
            // 添加 peer-couchdb container
            v1Containers.add(new V1Container()
                    .name(fabricConstructVo.getNodeName() + "-couchdb")
                    .image(BlockChainFabricImagesConstant.getFabricCouchDbImage(fabricConstructVo.getClusterVersion()))
                    .ports(new ArrayList<V1ContainerPort>() {{
                        add(new V1ContainerPort().containerPort(BlockChainFabricConstructConstant.COUCH_DB_PORT));
                    }})
                    .env(new ArrayList<V1EnvVar>() {{
                        add(new V1EnvVar().name("TZ").value("Asia/Shanghai"));
                        add(new V1EnvVar().name("COUCHDB_USER").value(BlockChainFabricConstructConstant.COUCH_DB_USERNAME));
                        add(new V1EnvVar().name("COUCHDB_PASSWORD").value(BlockChainFabricConstructConstant.COUCH_DB_PASSWORD));
                    }})
                    .volumeMounts(new ArrayList<V1VolumeMount>() {{
                        add(new V1VolumeMount().name("peer-data").mountPath("/opt/couchdb/data").subPath(peerDataPath + "/couchdb"));
                    }}));
        }

        // 添加peer container
        v1Containers.add(new V1Container()
                .name(fabricConstructVo.getNodeName())
                .image(fabricConstructVo.getImageName())
                .command(new ArrayList<String>() {{
                    add("/bin/bash");
                    add("-c");
                    add("peer node start");
                    // add("peer node start &>> /opt/log/logger.txt");
                }})
                .workingDir("/opt/gopath/src/github.com/hyperledger/fabric")
                .env(v1EnvVars)
                .ports(new ArrayList<V1ContainerPort>() {{
                    add(new V1ContainerPort().containerPort(fabricConstructVo.getNodePort()));
                    add(new V1ContainerPort().containerPort(fabricConstructVo.getChainCodePort()));
                    add(new V1ContainerPort().containerPort(fabricConstructVo.getMonitorPort()));
                }})
                .volumeMounts(new ArrayList<V1VolumeMount>() {{
                    add(new V1VolumeMount().name("peer-data").mountPath("/etc/hyperledger/fabric/msp").subPath(peerCertPath + "/msp"));
                    add(new V1VolumeMount().name("peer-data").mountPath("/etc/hyperledger/fabric/tls").subPath(peerCertPath + "/tls"));
                    add(new V1VolumeMount().name("peer-data").mountPath("/etc/hyperledger/fabric/orderer.yaml").subPath(fabricConstructVo.getNameSapce() + "/config/orderer.yaml"));
                    add(new V1VolumeMount().name("peer-data").mountPath("/etc/hyperledger/fabric/core.yaml").subPath(fabricConstructVo.getNameSapce() + "/config/core.yaml"));
                    add(new V1VolumeMount().name("peer-data").mountPath("/var/hyperledger/production").subPath(peerDataPath + "/production"));
                    add(new V1VolumeMount().name("peer-data").mountPath("/opt/log").subPath(peerDataPath + "/log"));
                    add(new V1VolumeMount().name("peer-docker").mountPath("/host/var/run/docker.sock"));
                }}));

        // 创建deployment
        V1PodSpec podSpec = new V1PodSpec()
                .hostAliases(new ArrayList<V1HostAlias>() {{
                    add(new V1HostAlias().ip("127.0.0.1").hostnames(Arrays.asList(fabricConstructVo.getNodeDomain())));
//                    add(new V1HostAlias().ip(nodeIp).hostnames(hostnames));
                }})
                .containers(v1Containers)
                .volumes(new ArrayList<V1Volume>() {{
                    add(new V1Volume()
                                    .name("peer-data")
//                            .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(K8sUtils.getPvcName(namespace)).readOnly(false))
                                    .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(BlockChainK8SConstant.getK8sPvcName(fabricConstructVo.getNameSapce())).readOnly(false))
                    );
                    add(new V1Volume()
                            .name("peer-docker")
                            .hostPath(new V1HostPathVolumeSource().path("/var/run/docker.sock")));
                }});
        K8SUtils.createDeployment(fabricConstructVo.getNameSapce(), fabricConstructVo.getNodeName(), labels, podSpec);

        String nodeTlsCaPath = fabricConstructVo.getCertPath() + "/" + fabricConstructVo.getNameSapce() + "/crypto-config/peerOrganizations/" + fabricConstructVo.getOrgName().toLowerCase() + "-" + fabricConstructVo.getNameSapce() + "/peers/" + fabricConstructVo.getNodeDomain() + "/tls/ca.crt";
        bcNode.setNodePort(fabricConstructVo.getNodePort());
        bcNode.setNodeIp(fabricConstructVo.getNodeDomain());
        bcNode.setNodeTlsPath(nodeTlsCaPath);
        return bcNode;
    }

}
