package org.bc.auto.utils;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.*;
import org.bc.auto.code.impl.K8SResultCode;
import org.bc.auto.config.BlockChainAutoConstant;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.exception.K8SException;
import org.bc.auto.model.entity.BCCert;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HyperledgerFabricComponentsStartUtils {

    private static final String APP_NAME = "auto-cluster";
    public static final int CA_PORT = 7054;
    public static final String ORDERER_MSP_SCRIPT_NAME = "generate-orderer-msp-certs.sh";
    public static final String ROOT_CA_LOGIN_INFO = "admin:adminpw";
    public static final String ORDERER_TLS_SCRIPT_NAME = "generate-orderer-tls-certs.sh";
    public static final String PEER_MSP_SCRIPT_NAME = "generate-peer-msp-certs.sh";
    public static final String PEER_TLS_SCRIPT_NAME = "generate-peer-tls-certs.sh";

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


    public static BCCert generateOrgCerts(String clusterName, BCCluster bcCluster, BCOrg bcOrg) {
        // 在k8s集群内部采用 svc.namespace 的方式来进行内部访问
        String mspCaUrl = BlockChainAutoConstant.MSP_CA_NAME + "." + clusterName + ":" + CA_PORT;
        String tlsCaUrl = BlockChainAutoConstant.TLS_CA_NAME + "." + clusterName + ":" + CA_PORT;

        String scriptsPath = BlockChainK8SConstant.getFabricOperateScriptsPath();
        String mspScriptPath = BlockChainK8SConstant.getFabricCaClientMspConfigFilePath(bcCluster.getClusterVersion());
        String tlsScriptPath = BlockChainK8SConstant.getFabricCaClientTlsConfigFilePath(bcCluster.getClusterVersion());
        String certsRootPath = BlockChainK8SConstant.getGenerateCertsPath();
        String saveCertsRootPath = BlockChainK8SConstant.getSaveCertsPath();

        //如果组织类型是Orderer的组织
        //如果是Orderer的组织类型的话，仅仅需要调用脚本生成Orderer组织的证书，以及Orderer用户的证书。
        //由于Orderer是在通道创建前生成的，所以不需要加入系统通道。
        //脚本执行两次，一次是MSP的证书，一次是TLS的证书。
        //如果是Peer类型的组织，则需要加入系统通道。
        switch (bcOrg.getOrgType()){
            case 1:{
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
                break;
            }
            case 2:{
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
            }
            default:{
                throw new K8SException(K8SResultCode.SHELL_EXEC_TYPE_ERROR);
            }
        }



        /** for (int i = 0; i < ordererNum; i++) {
            if (!ShellUtils.exec(scriptsPath+ORDERER_MSP_SCRIPT_NAME, "orderer", clusterName, "orderer" + i, ROOT_CA_LOGIN_INFO, mspCaUrl, scriptsPath, mspScriptPath, certsRootPath, saveCertsRootPath)) {
                logger.error("generate orderer node [{}] msp certs error.", "orderer" + i);
                throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
            }
        } */


        /** for (int i = 0; i < ordererNum; i++) {
            if (!ShellUtils.exec(scriptsPath + ORDERER_TLS_SCRIPT_NAME, "orderer", clusterName, "orderer" + i, ROOT_CA_LOGIN_INFO, tlsCaUrl, scriptsPath, tlsScriptPath, certsRootPath, saveCertsRootPath)) {
                logger.error("generate orderer node [{}] tls certs error.", "orderer" + i);
                throw new K8SException(K8SResultCode.SHELL_EXEC_ERROR);
            }
        }*/

        // 上传orderer管理员相关的证书、私钥至文件服务
//        String ordererCertPath = saveCertsRootPath + File.separator + clusterName + "/crypto-config/ordererOrganizations/" + clusterName + "/users/Admin@" + clusterName;


        BCCert bcCert = new BCCert();
        bcCert.setId(StringUtils.getId());
        bcCert.setCertName("Admin");
        bcCert.setCertType(1);
        bcCert.setCertStatus(1);
//        bcCert.setCertPriKey();

//        CertUser certUser = new CertUser();
//        certUser.setId(1);
//        certUser.setOrgId(orgOrderer.getId());
//        certUser.setOrgName(orgOrderer.getOrgName());
//        certUser.setCertUserName("Admin");
//        certUser.setCertUserType(1);
//        certUser.setOrgType(2);
//        certUser.setClusterId(cluster.getId());
////        certUser.setCertUserPubKey(certUserPubKey);
////        certUser.setCertUserPriKey(certUserPriKey);
////        certUser.setCertUserCaCert(certUserCaCert);
////        certUser.setCertTlsPubKey(certTlsPubKey);
////        certUser.setCertTlsPriKey(certTlsPriKey);
//        certUser.setCreateTime(DateUtils.currentTimeMillis());
//        String uuid = UUID.randomUUID().toString();
//        MemoryDataBase.setCertUser(uuid,certUser);
        return bcCert;
    }


}
