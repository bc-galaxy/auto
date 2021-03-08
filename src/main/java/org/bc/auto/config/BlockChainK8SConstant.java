package org.bc.auto.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;

public class BlockChainK8SConstant {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainK8SConstant.class);


    /**
     * K8S的连接配置文件路径，此路径下拥有K8S连接证书、权限等。
     * 注意：连接的证书需要有操控K8S的权限（需要创建POD）
     *
     * 一般默认的是 '～/.kube/config' 文件
     */
    private static String K8S_CONFIG_PATH = String.join(File.separator,"{0}","kube","config");

    /**
     * 生成的证书路径，当bc-auto创建区块链网络、节点、组织、用户等情况下，
     * 需要生成证书，此目录即为存放这些证书的路径。
     *
     * 当需要核验生成的证书文件，请至该目录查找。
     */
    private static String ROOT_PATH = String.join(File.separator,"{0}");
    private static String FABRIC_TOOLS_PATH = String.join(File.separator,"{0}","bin","{1}");
    private static String FABRIC_CONFIG_PATH = String.join(File.separator,"{0}","config/");
    private static String FABRIC_OPERATE_SCRIPTS_PATH = String.join(File.separator,"{0}","script/");

    //fabric-ca-client-msp-path
    private static String FABRIC_CA_CLIENT_MSP_CONFIG_FILE_PATH = String.join(File.separator,"{0}","bin","{1}","msp/");
    private static String FABRIC_CA_MSP_SERVER_URL = String.join(".","{0}","{1}");
    //fabric-ca-client-msp-path
    private static String FABRIC_CA_CLIENT_TLS_CONFIG_FILE_PATH = String.join(File.separator,"{0}","bin","{1}","tls/");
    private static String FABRIC_CA_TLS_SERVER_URL = String.join(".","{0}","{1}");

    private static String K8S_PV_NAME = String.join("-","{0}","{1}");
    private static String K8S_VOLUME_DATA_NAME = String.join("-","{0}","{1}");
    private static String K8S_PVC_NAME = String.join("-","{0}","{1}");
    private static String K8S_PDB_NAME = String.join("-","{0}","{1}");


    //get the url of fabric msp ca server url
    public static String getFabricCaMspServerUrl(String clusterName,int port){
        String fabricCaMspServerUrl = MessageFormat.format(FABRIC_CA_MSP_SERVER_URL,BlockChainAutoConstant.MSP_CA_NAME,clusterName+":"+port);
        logger.info("fabric msp ca k8s's pod svc url is '{}'",fabricCaMspServerUrl);
        return fabricCaMspServerUrl;
    }
    //get the url of fabric tls ca server url
    public static String getFabricCaTlsServerUrl(String clusterName,int port){
        String fabricCaTlsServerUrl = MessageFormat.format(FABRIC_CA_TLS_SERVER_URL,BlockChainAutoConstant.TLS_CA_NAME,clusterName+":"+port);
        logger.info("fabric tls ca k8s's pod svc url is '{}'",fabricCaTlsServerUrl);
        return fabricCaTlsServerUrl;
    }

    public static String getK8sConfigPath(){
        String k8sConfigPath = MessageFormat.format(K8S_CONFIG_PATH,getWorkPath());
        logger.info("k8s config file path is {}",k8sConfigPath);
        return k8sConfigPath;
    }

    public static String getWorkPath(){
        String generateCertsPath = MessageFormat.format(ROOT_PATH,BlockChainAutoConstant.K8S_WORK_PATH);
        logger.info("generate cert file path is {}",generateCertsPath);
        return generateCertsPath;
    }

    public static String getSavePath(){
        String generateCertsPath = MessageFormat.format(ROOT_PATH,BlockChainAutoConstant.K8S_DATA_PATH);
        logger.info("generate cert file path is {}",generateCertsPath);
        return generateCertsPath;
    }

    public static String getFabricToolsPath(String versionString){
        String fabricToolsPath = MessageFormat.format(FABRIC_TOOLS_PATH,getWorkPath(),versionString);
        logger.info("fabric tools file path is {}",fabricToolsPath);
        return fabricToolsPath;
    }

    public static String getFabricConfigPath(){
        String fabricConfigPath = MessageFormat.format(FABRIC_CONFIG_PATH,getWorkPath());
        logger.info("fabric config file path is {}",fabricConfigPath);
        return fabricConfigPath;
    }

    //FABRIC_OPERATE_SCRIPTS_PATH
    public static String getFabricOperateScriptsPath(){
        String fabricOperateScriptsPath = MessageFormat.format(FABRIC_OPERATE_SCRIPTS_PATH,getWorkPath());
        logger.info("fabric script file path is {}",fabricOperateScriptsPath);
        return fabricOperateScriptsPath;
    }

    //FABRIC_CA_CLIENT_MSP_CONFIG_FILE_PATH
    public static String getFabricCaClientMspConfigFilePath(String versionString){
        String fabricCaClientMspConfigFilePath = MessageFormat.format(FABRIC_CA_CLIENT_MSP_CONFIG_FILE_PATH,getWorkPath(),versionString);
        logger.info("fabric ca client msp config file path is {}",fabricCaClientMspConfigFilePath);
        return fabricCaClientMspConfigFilePath;
    }

    //FABRIC_CA_CLIENT_TLS_CONFIG_FILE_PATH
    public static String getFabricCaClientTlsConfigFilePath(String versionString){
        String fabricCaClientTlsConfigFilePath = MessageFormat.format(FABRIC_CA_CLIENT_TLS_CONFIG_FILE_PATH,getWorkPath(),versionString);
        logger.info("fabric ca client client config file path is {}",fabricCaClientTlsConfigFilePath);
        return fabricCaClientTlsConfigFilePath;
    }

    public static String getK8sPvName(String pvName){
        String k8sPvName = MessageFormat.format(K8S_PV_NAME,pvName,BlockChainAutoConstant.PV_SUFFIX);
        logger.info("K8S pv Name is {}",k8sPvName);
        return k8sPvName;
    }

    public static String getK8sVolumeDataName(String volumeDataName){
        String k8sVolumeDataName = MessageFormat.format(K8S_VOLUME_DATA_NAME,volumeDataName,BlockChainAutoConstant.VOLUME_DATA_SUFFIX);
        logger.info("K8S volume Data Name is {}",k8sVolumeDataName);
        return k8sVolumeDataName;
    }

    public static String getK8sPvcName(String pvcName){
        String k8sPvcName = MessageFormat.format(K8S_PVC_NAME,pvcName,BlockChainAutoConstant.PVC_SUFFIX);
        logger.info("K8S pvc Name is {}",k8sPvcName);
        return k8sPvcName;
    }

    public static String getK8sPdbName(String pdbName){
        String k8sPdbName = MessageFormat.format(K8S_PDB_NAME,pdbName,BlockChainAutoConstant.PDB_SUFFIX);
        logger.info("K8S pdb Name is {}",k8sPdbName);
        return k8sPdbName;
    }


}
