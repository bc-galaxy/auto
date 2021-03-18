package org.bc.auto.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class BlockChainFabricImagesConstant {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainFabricImagesConstant.class);

    /**
     * 确定fabric区块链组件的镜像前缀是以 hyperledger 开头
     *
     * 如果有自定义的镜像地址，可以改前缀地址来变为自己打包的镜像
     */
    private final static String FABRIC_IMAGE_PREFIX="hyperledger";

    /**
     * 确定fabric区块链组件Orderer的镜像名称
     *
     * 最终字符串结果为 "hyperledger/fabric-orderer:{0}"，其中占位符为版本信息
     */
    private static String FABRIC_ORDERE_IMAGE=String.join("/",FABRIC_IMAGE_PREFIX,"fabric-orderer:{0}");

    /**
     * 确定fabric区块链组件Peer的镜像名称
     *
     * 最终字符串结果为 "hyperledger/fabric-peer:{0}"，其中占位符为版本信息
     */
    private final static String FABRIC_PEER_IMAGE=String.join("/",FABRIC_IMAGE_PREFIX,"fabric-peer:{0}");

    /**
     * 确定fabric区块链组件Peer的镜像名称
     *
     * 最终字符串结果为 "hyperledger/fabric-ca:{0}"，其中占位符为版本信息
     *
     * 注意：Fabric的ca镜像为重新打包的镜像。指定了默认的启动命令、以及配置文件，方便K8S启动ca命令。
     */
//    private final static String FABRIC_CA_IMAGE=String.join("/",FABRIC_IMAGE_PREFIX,"fabric-ca-ml:{0}");
    private final static String FABRIC_CA_IMAGE=String.join("/",FABRIC_IMAGE_PREFIX,"fabric-ca:{0}");

    /**
     * 确定fabric区块链组件couchdb的镜像名称
     *
     * 最终字符串结果为 "hyperledger/fabric-couchdb:{0}"，其中占位符为版本信息
     *
     * 注意：couchdb的版本与Fabric的镜像版本不一致；经过确认couchdb的镜像版本与官方可以保持不一致。
     * 如果couchdb报错，可能是couchdb版本不一致造成的。
     */
    private final static String FABRIC_COUCH_DB_IMAGE=String.join("/",FABRIC_IMAGE_PREFIX,"fabric-couchdb:0.4.18");

    /**
     * 根据传入的镜像版本参数，进行最终Orderer镜像确定
     * @param versionString
     * @return
     */
    public static String getFabricOrdereImage(String versionString){
        logger.debug("[config->image]传入的Orderer镜像版本信息是:{}",versionString);
        String fabricOrdererImage = MessageFormat.format(FABRIC_ORDERE_IMAGE,versionString);
        logger.info("[config->image]确定启动Orderer镜像信息是:{}",fabricOrdererImage);
        return fabricOrdererImage;
    }

    /**
     * 根据传入的镜像版本参数，进行最终Peer镜像确定
     * @param versionString
     * @return
     */
    public static String getFabricPeerImage(String versionString){
        logger.debug("[config->image]传入的Peer镜像版本信息是:{}",versionString);
        String fabricPeerImage = MessageFormat.format(FABRIC_PEER_IMAGE,versionString);
        logger.info("[config->image]确定启动Peer镜像信息是:{}",fabricPeerImage);
        return fabricPeerImage;
    }

    /**
     * 根据传入的镜像版本参数，进行最终Ca镜像确定
     * @param versionString
     * @return
     */
    public static String getFabricCaImage(String versionString){
        logger.debug("[config->image]传入的Ca镜像版本信息是:{}",versionString);
        String fabricCaImage = MessageFormat.format(FABRIC_CA_IMAGE,versionString);
        logger.info("[config->image]确定启动Ca镜像信息是:{}",fabricCaImage);
        return fabricCaImage;
    }

    /**
     * 根据传入的镜像版本参数，进行最终Ca镜像确定
     * @param versionString
     * @return
     */
    public static String getFabricCouchDbImage(String versionString){
        logger.debug("[config->image]传入的couchDb镜像版本信息是:{}",versionString);
        String fabricCouchDbImage = MessageFormat.format(FABRIC_COUCH_DB_IMAGE,versionString);
        logger.info("[config->image]确定启动couchDb镜像信息是:{}",fabricCouchDbImage);
        return fabricCouchDbImage;
    }


}
