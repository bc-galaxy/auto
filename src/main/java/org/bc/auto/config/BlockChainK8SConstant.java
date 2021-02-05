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
    private static String GENERATE_CERTS_PATH = String.join(File.separator,"{0}","certs");
    private static String FABRIC_TOOLS_PATH = String.join(File.separator,"{0}","bin","{1}");
    private static String FABRIC_CONFIG_PATH = String.join(File.separator,"{0}","config");
    private static String FABRIC_OPERATE_SCRIPTS_PATH = String.join(File.separator,"{0}","scripts");

    //fabric-ca-client-msp-path
    private static String FABRIC_CA_CLIENT_MSP_CONFIG_FILE_PATH = String.join(File.separator,"{0}","bin","{1}","msp");
    //fabric-ca-client-msp-path
    private static String FABRIC_CA_CLIENT_TLS_CONFIG_FILE_PATH = String.join(File.separator,"{0}","bin","{1}","tls");



    public static String getK8sConfigPath(){
        String k8sConfigPath = MessageFormat.format(K8S_CONFIG_PATH,BlockChainAutoConstant.K8S_WORK_PATH);
        logger.info("k8s config file path is {}",k8sConfigPath);
        return k8sConfigPath;
    }

    public static String getGenerateCertsPath(){
        String generateCertsPath = MessageFormat.format(GENERATE_CERTS_PATH,BlockChainAutoConstant.K8S_WORK_PATH);
        logger.info("generate cert file path is {}",generateCertsPath);
        return generateCertsPath;
    }

    public static String getFabricToolsPath(String versionString){
        String fabricToolsPath = MessageFormat.format(FABRIC_TOOLS_PATH,BlockChainAutoConstant.K8S_WORK_PATH,versionString);
        logger.info("fabric tools file path is {}",fabricToolsPath);
        return fabricToolsPath;
    }

    public static String getFabricConfigPath(){
        String fabricConfigPath = MessageFormat.format(FABRIC_CONFIG_PATH,BlockChainAutoConstant.K8S_WORK_PATH);
        logger.info("fabric config file path is {}",fabricConfigPath);
        return fabricConfigPath;
    }

    //FABRIC_OPERATE_SCRIPTS_PATH
    public static String getFabricOperateScriptsPath(){
        String fabricOperateScriptsPath = MessageFormat.format(FABRIC_OPERATE_SCRIPTS_PATH,BlockChainAutoConstant.K8S_WORK_PATH);
        logger.info("fabric script file path is {}",fabricOperateScriptsPath);
        return fabricOperateScriptsPath;
    }

    //FABRIC_CA_CLIENT_MSP_CONFIG_FILE_PATH
    public static String getFabricCaClientMspConfigFilePath(String versionString){
        String fabricCaClientMspConfigFilePath = MessageFormat.format(FABRIC_CA_CLIENT_MSP_CONFIG_FILE_PATH,BlockChainAutoConstant.K8S_WORK_PATH,versionString);
        logger.info("fabric ca client msp config file path is {}",fabricCaClientMspConfigFilePath);
        return fabricCaClientMspConfigFilePath;
    }

    //FABRIC_CA_CLIENT_TLS_CONFIG_FILE_PATH
    public static String getFabricCaClientTlsConfigFilePath(String versionString){
        String fabricCaClientTlsConfigFilePath = MessageFormat.format(FABRIC_CA_CLIENT_TLS_CONFIG_FILE_PATH,BlockChainAutoConstant.K8S_WORK_PATH,versionString);
        logger.info("fabric ca client client config file path is {}",fabricCaClientTlsConfigFilePath);
        return fabricCaClientTlsConfigFilePath;
    }


}
