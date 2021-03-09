package org.bc.auto.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.catalina.core.ApplicationContext;
import org.bc.auto.code.impl.K8SResultCode;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.config.BlockChainAutoConstant;
import org.bc.auto.config.BlockChainFabricImagesConstant;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.exception.K8SException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.model.entity.BlockChainNetwork;
import org.bc.auto.model.entity.BlockChainNodeList;
import org.bc.auto.service.ClusterService;
import org.bc.auto.service.NodeService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class BlockChainNetworkClusterListener implements BlockChainListener{

    private static final Logger logger = LoggerFactory.getLogger(BlockChainNetworkClusterListener.class);

    @Override
    public void doEven(BlockChainEven blockChainEven) {
        ThreadPoolManager.newInstance().addExecuteTask(new Runnable() {
            @Override
            public void run() {
                try{
                    //获取需要创建的集群对象
                    BCCluster bcCluster = (BCCluster)blockChainEven.getBlockChainNetwork();

                    //创建集群对应的pv,目前支持nfs的方式。
                    //规则："集群名称"+"-pv"
                    K8SUtils.createPersistentVolume(BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()), BlockChainAutoConstant.NFS_HOST,BlockChainAutoConstant.NFS_PATH,"100Mi");
                    logger.info("[async] create k8s pv name, name is :{}, storage size is {}",BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi");

                    //创建对应的命名空间
                    //规则：集群名称
                    K8SUtils.createNamespace(bcCluster.getClusterName());
                    logger.info("[async] create k8s name space, name is {}",bcCluster.getClusterName());

                    //创建对应的pvc，目前支持nfs的方式
                    //规则："集群名称"+"-pvc"
                    K8SUtils.createPersistentVolumeClaim(bcCluster.getClusterName(),BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi");
                    logger.info("[async] create k8s pvc name, name is :{}, storage size is {}",BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),"100Mi");

                    //启动CA服务器命令
                    String command = "bash /opt/start-rca.sh admin:adminpw";
                    //启动MSP的根CA服务器
                    HyperledgerFabricComponentsStartUtils.setupCa(bcCluster.getClusterName(),BlockChainAutoConstant.MSP_CA_NAME, BlockChainFabricImagesConstant.getFabricCaImage(bcCluster.getClusterVersion()),command);
                    //启动TLS的根CA服务器
                    HyperledgerFabricComponentsStartUtils.setupCa(bcCluster.getClusterName(),BlockChainAutoConstant.TLS_CA_NAME, BlockChainFabricImagesConstant.getFabricCaImage(bcCluster.getClusterVersion()),command);
                    //如果最终检查ca的服务器都没有启动的话，就抛出异常
                    if(!K8SUtils.checkPodStatus(bcCluster.getClusterName())){
                        logger.error("[async] create msp ca or tls error, k8s's name space is {}",bcCluster.getClusterName());
                        throw new K8SException(K8SResultCode.CHECK_POD_ERROR);
                    }


                    //CA服务完全启动之后，当前线程沉睡5秒。确保服务能正常提供服务
                    Thread.sleep(5000L);

                    OrgService orgService = SpringBeanUtil.getBean(OrgService.class);
                    //CA启动完成，调用组织服务
                    //默认是orderer组织，orderer组织不需要用户手动创建
                    //orderer组织的参数在创建集群的时候应该确定
                    JSONObject ordererJsonObject = new JSONObject();
                    ordererJsonObject.put("clusterId",bcCluster.getId());
                    ordererJsonObject.put("orgName","Orderer");
                    ordererJsonObject.put("orgIsTls",1);
                    ordererJsonObject.put("orgType",1);
                    //得到Orderer组织的返回结果
                    BCOrg bcOrg = orgService.createOrg(ordererJsonObject);
                    ValidatorUtils.isNotNull(bcOrg);
                    logger.info("创建集群,Orderer组织创建成功");
                    //创建Orderer节点
                    //组织orderer节点列表的参数
                    JSONArray jsonArray = new JSONArray();
                    NodeService nodeService = SpringBeanUtil.getBean(NodeService.class);
                    //确定orderer节点的次数
                    for(int i=0 ; i<bcCluster.getOrdererCount() ;i++){
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("clusterId",bcCluster.getId());
                        jsonObject.put("nodeName","orderer"+i);
                        jsonObject.put("nodeType",1);
                        jsonObject.put("orgId",bcOrg.getId());
                        jsonObject.put("orgName",bcOrg.getOrgName());
                        jsonArray.add(jsonObject);
                    }
                    BlockChainNodeList blockChainNodeList = nodeService.createNode(jsonArray);
                    boolean flag = BlockChainShellQueueUtils.add(blockChainNodeList);
                    if(!flag){
                        logger.error("[node->create] 组织加入任务队列错误，请确认错误信息。");
                        throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_QUEUE_ERROR);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    logger.error("[async] create cluster error, error info is {}",e.getMessage());
                    throw new K8SException();
                }
            }

        });
    }
}
