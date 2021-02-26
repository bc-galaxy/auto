package org.bc.auto.utils;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import org.bc.auto.code.impl.K8SResultCode;
import org.bc.auto.config.BlockChainFabricImagesConstant;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.exception.K8SException;
import org.bc.auto.model.entity.BCCert;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.model.entity.BCOrg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

public class HyperledgerFabricComponentsStartUtils {

    private static final String APP_NAME = "auto-cluster";
    public static final int CA_PORT = 7054;
    public static final String ORDERER_MSP_SCRIPT_NAME = "generate-orderer-msp-certs.sh";
    public static final String ROOT_CA_LOGIN_INFO = "admin:adminpw";
    public static final String ORDERER_TLS_SCRIPT_NAME = "generate-orderer-tls-certs.sh";
    public static final String PEER_MSP_SCRIPT_NAME = "generate-peer-msp-certs.sh";
    public static final String PEER_TLS_SCRIPT_NAME = "generate-peer-tls-certs.sh";
    public static final String ADD_ORG_TO_SYS_CHANNEL = "add-org-to-syschannel.sh";
    public static final String GENERATE_GENESIS_SCRIPT_NAME = "generate-genesis.sh";
    public static final String COMMON_OPERATE_SCRIPT_NAME = "common-operate.sh";

    public static final int PEER_PORT = 7051;
    public static final int PEER_CHAINCODE_PORT = 7052;
    public static final int COUCH_DB_PORT = 5984;
    public static final String COUCH_DB_USERNAME = "auto-cluster-username";
    public static final String COUCH_DB_PASSWORD = "auto-cluster-password";
    public static final int ORDERER_PORT = 7050;
    public static final int ORDERER_MONITOR_PORT = 8443;

    private static final Logger logger = LoggerFactory.getLogger(HyperledgerFabricComponentsStartUtils.class);

    public static void setupCa(String namespace, String name, String image, String command) {

        final Map<String, String> labels = new HashMap<String, String>(4) {{
            put("namespace", namespace);
            put("app", APP_NAME);
            put("role", "ca");
            put("ca-name", name);
        }};

        // 创建根msp ca deployment
        V1PodSpec podSpec = new V1PodSpec()
                .containers(new ArrayList<V1Container>() {{
                    add(new V1Container()
                                    .name(name)
                                    .image(image)
                                    .command(new ArrayList<String>() {{
                                        add("/bin/bash");
                                        add("-c");
                                        add(command);
                                    }})
                                    .env(new ArrayList<V1EnvVar>() {{
                                        add(new V1EnvVar().name("TZ").value("Asia/Shanghai"));
                                        add(new V1EnvVar().name("FABRIC_CA_HOME").value("/etc/hyperledger/fabric-ca-server"));
                                    }})
                                    .ports(new ArrayList<V1ContainerPort>() {{
                                        add(new V1ContainerPort().containerPort(CA_PORT));
                                    }})
                                    .volumeMounts(new ArrayList<V1VolumeMount>() {{
                                        add(new V1VolumeMount().name("ca-data").mountPath("/etc/hyperledger/fabric-ca-server").subPath(namespace + "/" + name));
//                                add(new V1VolumeMount().name("ca-config").mountPath("/opt").subPath("/fabric-ca-server-config-rca.yaml"));
//                                add(new V1VolumeMount().name("ca-start").mountPath("/opt").subPath("/start-rca.sh"));
                                    }})
                    );
                }})
                .volumes(new ArrayList<V1Volume>() {{
                    add(new V1Volume()
                            .name("ca-data")
                            .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName(BlockChainK8SConstant.getK8sPvcName(namespace)))
                    );
                    /*add(new V1Volume()
                            .name("ca-config")
                            .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName("/data/share/fabric-ca/fabric-ca-server-config-rca.yaml"))
                    );
                    add(new V1Volume()
                            .name("ca-start")
                            .persistentVolumeClaim(new V1PersistentVolumeClaimVolumeSource().claimName("/data/share/fabric-ca/start-rca.sh"))
                    );*/
                }});
        K8SUtils.createDeployment(namespace, name, labels, podSpec);
        logger.info("create deployment {} successful.", name);

        // 创建根ca service
        V1ServiceSpec svcSpec = new V1ServiceSpec()
                .selector(labels)
                .ports(new ArrayList<V1ServicePort>() {{
                    add(new V1ServicePort().name("listen").port(CA_PORT).targetPort(new IntOrString(CA_PORT)));
                }});
        K8SUtils.createService(namespace, name, labels, svcSpec);
        logger.info("create service {} successful.", name);
    }

    public static void generateNodeCerts(BCCluster bcCluster, BCNode bcNode) {
        String clusterName = bcCluster.getClusterName();
        // 在k8s集群内部采用 svc.namespace 的方式来进行内部访问
        String mspCaUrl = BlockChainK8SConstant.getFabricCaMspServerUrl(clusterName,CA_PORT);
        String tlsCaUrl = BlockChainK8SConstant.getFabricCaTlsServerUrl(clusterName,CA_PORT);

        String scriptsPath = BlockChainK8SConstant.getFabricOperateScriptsPath();
        String mspScriptPath = BlockChainK8SConstant.getFabricCaClientMspConfigFilePath(bcCluster.getClusterVersion());
        String tlsScriptPath = BlockChainK8SConstant.getFabricCaClientTlsConfigFilePath(bcCluster.getClusterVersion());
        String certsRootPath = BlockChainK8SConstant.getGenerateCertsPath();
        String saveCertsRootPath = BlockChainK8SConstant.getSaveCertsPath();

        switch (bcNode.getNodeType()){
            case 1:{
                if (!ShellUtils.exec(scriptsPath+ORDERER_MSP_SCRIPT_NAME, "orderer", clusterName, bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("generate orderer node [{}] msp certs error.", bcNode.getNodeName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + ORDERER_TLS_SCRIPT_NAME, "orderer", clusterName, bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("generate orderer node [{}] tls certs error.", bcNode.getNodeName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                break;
            }
            case 2:{
                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "register_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("register peer -> {} for org -> {} through msp ca error.", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "enroll_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("enroll peer -> {} for org -> {} through msp ca error.", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "register_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("register peer -> {} for org -> {} through tls ca error.", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "enroll_peer", clusterName, bcNode.getOrgName(), bcNode.getNodeName(), ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("enroll peer -> {} for org -> {} through tls ca error.", bcNode.getNodeName(), bcNode.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                break;
            }
            default:{
                throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
            }
        }

    }

    public static BCCert generateOrgCerts(BCCluster bcCluster, BCOrg bcOrg) {
        // 在k8s集群内部采用 svc.namespace 的方式来进行内部访问
        String clusterName = bcCluster.getClusterName();
        String mspCaUrl = BlockChainK8SConstant.getFabricCaMspServerUrl(clusterName,CA_PORT);
        String tlsCaUrl = BlockChainK8SConstant.getFabricCaTlsServerUrl(clusterName,CA_PORT);
        BCCert bcCert = new BCCert();

        String scriptsPath = BlockChainK8SConstant.getFabricOperateScriptsPath();
        String mspScriptPath = BlockChainK8SConstant.getFabricCaClientMspConfigFilePath(bcCluster.getClusterVersion());
        String tlsScriptPath = BlockChainK8SConstant.getFabricCaClientTlsConfigFilePath(bcCluster.getClusterVersion());
        String certsRootPath = BlockChainK8SConstant.getGenerateCertsPath();
        String saveCertsRootPath = BlockChainK8SConstant.getSaveCertsPath();

        String certUserCaCert;
        String certUserPubKey;
        String certUserPriKey;
        String certTlsPubKey;
        String certTlsPriKey;

        //如果组织类型是Orderer的组织
        //如果是Orderer的组织类型的话，仅仅需要调用脚本生成Orderer组织的证书，以及Orderer用户的证书。
        //由于Orderer是在通道创建前生成的，所以不需要加入系统通道。
        //脚本执行两次，一次是MSP的证书，一次是TLS的证书。
        //如果是Peer类型的组织，则需要加入系统通道。
        //Orderer组织自动生成orderer的节点
        switch (bcOrg.getOrgType()){
            case 1:{
                //确定orderer证书的存储路径前缀
                String ordererCertPath = saveCertsRootPath + File.separator + clusterName + "/crypto-config/ordererOrganizations/" + clusterName + "/users/Admin@" + clusterName;
                // 生成orderer msp信息
                if (!ShellUtils.exec(scriptsPath+ORDERER_MSP_SCRIPT_NAME, "ordererOrg", clusterName, "orderer0", ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("generate orderer admin msp certs error.");
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }

                // 生成orderer tls信息
                if (!ShellUtils.exec(scriptsPath + ORDERER_TLS_SCRIPT_NAME, "ordererOrg", clusterName, "orderer0", ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("generate orderer admin tls certs error.");
                    throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
                }
                certUserCaCert = ordererCertPath + "/msp/cacerts/ca." + clusterName + "-cert.pem";
                certUserPubKey = ordererCertPath + "/msp/signcerts/Admin@" + clusterName + "-cert.pem";
                certUserPriKey = new File(ordererCertPath + "/msp/keystore").listFiles()[0].getPath();
                certTlsPubKey = ordererCertPath + "/tls/client.crt";
                certTlsPriKey = ordererCertPath + "/tls/client.key";
                bcCert.setCertType(1);
                break;
            }
            case 2:{
                //节点的证书路径前缀
                String adminCertPath = saveCertsRootPath + File.separator + clusterName + "/crypto-config/peerOrganizations/" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName + "/users/Admin@" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName;

                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "register_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("register admin user for org -> {} through msp ca error.", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_MSP_SCRIPT_NAME, "enroll_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("enroll admin user for org -> {} through msp ca error.", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                logger.info("Register and enroll org -> {} admin msp certs successfully.", bcOrg.getOrgName());

                // 生成org tls信息
                logger.info("Register and enroll org -> {} admin tls certs begin...", bcOrg.getOrgName());

                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "register_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("register admin user for org -> {} through tls ca error.", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                if (!ShellUtils.exec(scriptsPath + PEER_TLS_SCRIPT_NAME, "enroll_admin_user", clusterName, bcOrg.getOrgName(), "peer0", ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                    logger.error("enroll admin user for org -> {} through tls ca error.", bcOrg.getOrgName());
                    throw new K8SException(K8SResultCode.SHELL_EXEC_PEER_ORG_CERT_ERROR);
                }
                certUserCaCert = adminCertPath + "/msp/cacerts/ca." + clusterName + "-cert.pem";
                certUserPubKey = adminCertPath + "/msp/signcerts/Admin@" + bcOrg.getOrgName().toLowerCase() + "-" + clusterName + "-cert.pem";
                certUserPriKey = new File(adminCertPath + "/msp/keystore").listFiles()[0].getPath();
                certTlsPubKey = adminCertPath + "/tls/client.crt";
                certTlsPriKey = adminCertPath + "/tls/client.key";
                bcCert.setCertType(3);
                // 将组织MSP动态添加至系统通道内
                if (!ShellUtils.exec(scriptsPath + ADD_ORG_TO_SYS_CHANNEL, clusterName, bcOrg.getOrgName(), "Orderer", "orderer0", BlockChainK8SConstant.getFabricToolsPath(bcCluster.getClusterVersion()), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSaveCertsPath())) {
                    logger.error("add new org -> {} to system channel: {} error.", bcOrg.getOrgName(), clusterName);
                    throw new K8SException();
                }

                break;
            }
            default:{
                throw new K8SException(K8SResultCode.SHELL_EXEC_TYPE_ERROR);
            }
        }

        bcCert.setId(StringUtils.getId());
        bcCert.setCertName("Admin");

        bcCert.setCertStatus(1);
        bcCert.setCertPubKey(certUserPubKey);
        bcCert.setCertPriKey(certUserPriKey);
        bcCert.setCertCaCert(certUserCaCert);
        bcCert.setCertTlsPriKey(certTlsPriKey);
        bcCert.setCertPubKey(certTlsPubKey);
        bcCert.setOrgId(bcOrg.getId());

        return bcCert;
    }

    private static Map<String,Integer> createOrdererService(String clusterName,int ordererNum){
        LinkedHashMap<String, Integer> ordererNodePorts = new LinkedHashMap<>();
        for (int i = 0; i < ordererNum; i++) {
            final String ordererName = "orderer" + i;
            final Map<String, String> labels = new HashMap<String, String>(4) {{
                put("namespace", clusterName);
                put("app", APP_NAME);
                put("role", "orderer");
                put("orderer-name", ordererName);
            }};

            // 创建service
            V1ServiceSpec svcSpec = new V1ServiceSpec()
                    .type("NodePort")
                    .selector(labels)
                    .ports(new ArrayList<V1ServicePort>() {{
                        add(new V1ServicePort().name("listen").protocol("TCP").port(ORDERER_PORT).targetPort(new IntOrString(ORDERER_PORT)));
                        add(new V1ServicePort().name("monitor").protocol("TCP").port(ORDERER_MONITOR_PORT).targetPort(new IntOrString(ORDERER_MONITOR_PORT)));
                    }});
            K8SUtils.createService(clusterName, ordererName, labels, svcSpec);
            logger.info("create service {} successful.", ordererName);

            Integer nodePort = K8SUtils.queryNodePort(ordererName, clusterName);
            if (nodePort <= 0) {
                logger.error("query service -> {}'s node port from namespace -> {} error. it must be greater than 30000.", ordererName, clusterName);
                throw new K8SException();
            }
            ordererNodePorts.put(ordererName, nodePort);
        }
        return ordererNodePorts;
    }

    public static void buildFabricChain(BCCluster bcCluster,BCOrg bcOrg){
        // 将config目录拷贝至pv存储目录下
        if (!ShellUtils.exec(BlockChainK8SConstant.getFabricOperateScriptsPath() + COMMON_OPERATE_SCRIPT_NAME, "copy_config", bcCluster.getClusterName(), BlockChainK8SConstant.getFabricConfigPath(), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSaveCertsPath(), "")) {
            //证书拷贝失败
            logger.error("copy yaml config file [orderer.yaml & core.yaml] error.");
            throw new K8SException();
        }

        // 创建对应数量的orderer service，并拿到对应每个orderer serivce的NodePort
        Map<String, Integer> ordererNodePorts = createOrdererService(bcCluster.getClusterName(), bcCluster.getOrdererCount());

        // 根据共识类型组织orderer节点地址列表、kafka、raft、orderer节点个数等信息
        List<String> ordererAddressList = new ArrayList<>();
        List<String> kafkaAddressList = new ArrayList<>();
        List<Map<String, Object>> raftConsensus = new ArrayList<>();
        switch (bcCluster.getClusterConsensusType()) {
            case 1:
                for (int i = 0; i < bcCluster.getOrdererCount(); i++) {
                    // 准备ordererAddressList
                    ordererAddressList.add("orderer" + i + "." + bcCluster.getClusterName() + ":" + ordererNodePorts.get("orderer" + i));
                }
                break;
            case 3:
                for (int i = 0; i < bcCluster.getOrdererCount(); i++) {
                    // 准备ordererAddressList
                    ordererAddressList.add("orderer" + i + "." + bcCluster.getClusterName() + ":" + ordererNodePorts.get("orderer" + i));
                    // 准备raftConsensus
                    Map<String, Object> consensus = new LinkedHashMap<>();
                    consensus.put("Host", "orderer" + i + "." + bcCluster.getClusterName());
                    consensus.put("Port", ordererNodePorts.get("orderer" + i));
                    consensus.put("ClientTLSCert", "../../crypto-config/ordererOrganizations/" + bcCluster.getClusterName() + "/orderers/orderer" + i + "." + bcCluster.getClusterName() + "/tls/server.crt");
                    consensus.put("ServerTLSCert", "../../crypto-config/ordererOrganizations/" + bcCluster.getClusterName() + "/orderers/orderer" + i + "." + bcCluster.getClusterName() + "/tls/server.crt");
                    raftConsensus.add(consensus);
                }
                break;
            default:
                logger.error("Invalid consensus strategy. currently auto only support 'kafka' or 'raft' consenter type.");
                throw new K8SException();
        }

        // 生成configtx.yaml
        String ordererOrgMspDir = "../../crypto-config/ordererOrganizations/" + bcCluster.getClusterName().toLowerCase() + "/msp";
        String yamlFilePath = BlockChainK8SConstant.getSaveCertsPath() + File.separator + bcCluster.getClusterName().toLowerCase() + File.separator + "channels" + File.separator + bcCluster.getClusterName().toLowerCase() + File.separator + "configtx.yaml";
        if (!ConfigTxUtils.generateSysConfigTxYaml(3, bcOrg.getOrgName(), bcOrg.getOrgMspId(), ordererOrgMspDir, ordererAddressList, kafkaAddressList, raftConsensus, yamlFilePath)) {
//            MemoryDataBase.updateClusterStatus(3);
            throw new K8SException();
        }

        // 生成系统通道创世块genesis.block
        if (!ShellUtils.exec(BlockChainK8SConstant.getFabricOperateScriptsPath() + GENERATE_GENESIS_SCRIPT_NAME, bcCluster.getClusterName().toLowerCase(), bcCluster.getClusterName().toLowerCase(), BlockChainK8SConstant.getFabricToolsPath(bcCluster.getClusterVersion()), BlockChainK8SConstant.getFabricOperateScriptsPath(), BlockChainK8SConstant.getSaveCertsPath())) {
            logger.error("generate orderer genesis block by system channel -> {} error.", bcCluster.getClusterName());
//            MemoryDataBase.updateClusterStatus(3);
            throw new K8SException();
        }
    }
}
