package org.bc.auto.listener;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.K8SResultCode;
import org.bc.auto.config.BlockChainAutoConstant;
import org.bc.auto.config.BlockChainFabricImagesConstant;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.exception.K8SException;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BlockChainNetwork;
import org.bc.auto.service.ClusterService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BlockChainNetworkClusterListener implements BlockChainListener{

    private static final Logger logger = LoggerFactory.getLogger(BlockChainNetworkClusterListener.class);

    private OrgService orgService;
    @Autowired
    public void setOrgService(OrgService orgService) {
        this.orgService = orgService;
    }

    @Override
    public void doEven(BlockChainEven blockChainEven) {
        ThreadPoolManager.newInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                try{
                    BCCluster bcCluster = (BCCluster)blockChainEven.getBlockChainNetwork();

                    //创建对应的pv
                    K8SUtils.createPersistentVolume(BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()), BlockChainAutoConstant.NFS_HOST,BlockChainAutoConstant.NFS_PATH,"100Mi");
                    logger.info("[async] create k8s pv name, name is :{}, storage size is {}",BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi");

                    //创建对应的ns
                    K8SUtils.createNamespace(bcCluster.getClusterName());
                    logger.info("[async] create k8s name space, name is {}",bcCluster.getClusterName());

                    //创建对应的pvc
                    K8SUtils.createPersistentVolumeClaim(bcCluster.getClusterName(),BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi");
                    logger.info("[async] create k8s pvc name, name is :{}, storage size is {}",BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),"100Mi");

                    //启动CA服务器
                    String command = "bash /opt/start-rca.sh admin:adminpw";
                    //启动MSP的根CA服务器
                    HyperledgerFabricComponentsStartUtils.setupCa(bcCluster.getClusterName(),BlockChainAutoConstant.MSP_CA_NAME, BlockChainFabricImagesConstant.getFabricCaImage(bcCluster.getClusterVersion()),command);
                    HyperledgerFabricComponentsStartUtils.setupCa(bcCluster.getClusterName(),BlockChainAutoConstant.TLS_CA_NAME, BlockChainFabricImagesConstant.getFabricCaImage(bcCluster.getClusterVersion()),command);
                    //如果最终检查ca的服务器都没有启动的话，就抛出异常
                    if(!K8SUtils.checkPodStatus(bcCluster.getClusterName())){
                        logger.error("[async] create msp ca or tls error, k8s's name space is {}",bcCluster.getClusterName());
                        throw new K8SException(K8SResultCode.CHECK_POD_ERROR);
                    }

                    //CA启动完成，调用组织服务
                    JSONObject ordererJsonObject = new JSONObject();
                    ordererJsonObject.put("clusterId",bcCluster.getId());
                    ordererJsonObject.put("orgName","Orderer");
                    ordererJsonObject.put("orgIsTls",1);
                    ordererJsonObject.put("orgType",1);
                    orgService.createOrg(ordererJsonObject);
                }catch (Exception e){
                    logger.error("[async] create cluster error, error info is {}",e.getMessage());
                    throw new K8SException();
                }
            }

        });
    }
}
