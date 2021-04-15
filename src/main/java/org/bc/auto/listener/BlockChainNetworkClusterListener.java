package org.bc.auto.listener;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.BlockChainResultCode;
import org.bc.auto.code.impl.K8SResultCode;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.config.BlockChainAutoConstant;
import org.bc.auto.config.BlockChainFabricImagesConstant;
import org.bc.auto.config.BlockChainK8SConstant;
import org.bc.auto.exception.K8SException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.source.BlockChainFabricClusterEventSource;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.listener.source.BlockChainFabricNodeEventSource;
import org.bc.auto.service.NodeService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainNetworkClusterListener implements BlockChainListener{

    private static final Logger logger = LoggerFactory.getLogger(BlockChainNetworkClusterListener.class);

    @Override
    public void doEven(BlockChainEvent blockChainEven) {
        try{
            //获取需要创建的集群对象
            BlockChainFabricClusterEventSource blockChainFabricClusterEventSource =  (BlockChainFabricClusterEventSource)blockChainEven.getBlockChainEventSource();
            BCCluster bcCluster = blockChainFabricClusterEventSource.getBcCluster();
            //创建集群对应的pv,目前支持nfs的方式。
            //规则："集群名称"+"-pv"
            K8SUtils.createPersistentVolume(BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()), BlockChainAutoConstant.NFS_HOST,BlockChainAutoConstant.NFS_PATH,"100Mi");
            logger.debug("[async->cluster] create blockchain's cluster, this is to create k8s pv, name is :{}, storage size is {}",BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi");
            //创建对应的命名空间
            //规则：集群名称
            K8SUtils.createNamespace(bcCluster.getClusterName());
            logger.debug("[async->cluster] create blockchain's cluster, this is to create k8s namespace, name is :{}",bcCluster.getClusterName());
            //创建对应的pvc，目前支持nfs的方式
            //规则："集群名称"+"-pvc"
            K8SUtils.createPersistentVolumeClaim(bcCluster.getClusterName(),BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi");
            logger.debug("[async->cluster] create blockchain's cluster, this is to create k8s pvc, name is :{}, storage size is {}",BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),"100Mi");
            logger.info("[async->cluster] create blockchain's cluster, k8s pv name is :{}, pv storage size is {}, k8s namespace name is :{}, k8s pvc name is :{}, pvc storage size is {}",
                    BlockChainK8SConstant.getK8sPvName(bcCluster.getClusterName()),"100Mi",bcCluster.getClusterName(),BlockChainK8SConstant.getK8sPvcName(bcCluster.getClusterName()),"100Mi");
            //启动CA服务器命令
            String command = "bash /opt/start-rca.sh admin:adminpw";
            logger.debug("[async->cluster] create blockchain's cluster, this is to start ca server, command is '{}'",command);
            //启动MSP的根CA服务器
            HyperledgerFabricComponentsStartUtils.startHyperledgerFabricCaServer(bcCluster.getClusterName(),BlockChainAutoConstant.MSP_CA_NAME, BlockChainFabricImagesConstant.getFabricCaImage(bcCluster.getClusterVersion()),command);
            //启动TLS的根CA服务器
            HyperledgerFabricComponentsStartUtils.startHyperledgerFabricCaServer(bcCluster.getClusterName(),BlockChainAutoConstant.TLS_CA_NAME, BlockChainFabricImagesConstant.getFabricCaImage(bcCluster.getClusterVersion()),command);
            //如果最终检查ca的服务器都没有启动的话，就抛出异常
            if(!K8SUtils.checkPodStatus(bcCluster.getClusterName())){
                logger.error("[async->cluster] create blockchain's cluster, check ca server status pod error, make sure ca server start is success, namespace name is :{}",bcCluster.getClusterName());
                throw new K8SException(K8SResultCode.CHECK_POD_ERROR);
            }
            logger.info("[async->cluster] create blockchain's cluster, check ca server status pod running success, namespace name is :{}",bcCluster.getClusterName());
            //CA服务完全启动之后，当前线程沉睡5秒。确保服务加载完成，能正常提供证书相关服务
            //防止服务器启动过慢，造成pod状态为启动而服务正在加载的情况
            Thread.sleep(5000L);
            //Orderer组织的参数在创建集群的时候应该确定
            //创建集群的时候，需要默认的创建Orderer组织,Orderer组织不需要用户手动创建
            //添加以下Orderer组织相关的参数

            //通过SpringBeanUtil获取spring的service对象
            OrgService orgService = SpringBeanUtil.getBean(OrgService.class);
            JSONObject ordererJsonObject = new JSONObject();
            ordererJsonObject.put("clusterId",bcCluster.getId());
            ordererJsonObject.put("orgName","Orderer");
            ordererJsonObject.put("orgIsTls",1);
            ordererJsonObject.put("orgType",1);
            //得到Orderer组织的返回结果
            BCOrg bcOrg = orgService.createOrg(ordererJsonObject);
            logger.debug("[async->cluster] create blockchain's cluster, default to create Orderer org, cluster name is :{},org name is :{}, org type is :{}",
                    bcCluster.getClusterName(),bcOrg.getOrgName(),bcOrg.getOrgType());
            ValidatorUtils.isNotNull(bcOrg);
            logger.info("[async->cluster] create blockchain's cluster, default to create Orderer org success, cluster name is :{},org name is :{}",
                    bcCluster.getClusterName(),bcOrg.getOrgName());
            //创建集群的时候，需要默认的创建Orderer组织,Orderer组织不需要用户手动创建
            //创建完Orderer组织之后，需要创建orderer节点
            //创建Orderer节点，获取orderer节点列表相关信息
            JSONArray jsonArray = new JSONArray();
            NodeService nodeService = SpringBeanUtil.getBean(NodeService.class);
            //确定orderer节点的次数
            for(int i=0 ; i<bcCluster.getOrdererCount() ;i++){
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("clusterId",bcCluster.getId());
                //orderer节点名称规则：以orderer开头后面加节点目标数 如：orderer1、orderer2
                jsonObject.put("nodeName","orderer"+i);
                //此处固定为orderer节点，所以节点类型为1
                jsonObject.put("nodeType",1);
                jsonObject.put("orgId",bcOrg.getId());
                jsonObject.put("orgName",bcOrg.getOrgName());
                jsonArray.add(jsonObject);
                logger.debug("[async->cluster] create blockchain's cluster, default to create Orderer's node, cluster name is :{},node name is :{}, node type is :{}",
                        bcCluster.getClusterName(),"orderer"+i,1);
            }
            boolean flag = nodeService.createNode(jsonArray);
            logger.info("[async->cluster] create blockchain's cluster, default to create Orderer's node success, cluster name is :{},node count is :{}, node type is :{}",
                    bcCluster.getClusterName(),bcCluster.getOrdererCount(),1);
            if(!flag){
                logger.error("[async->cluster] create blockchain's cluster，default to create Orderer's node error, join shell queue error, this leads to cant ask for node certs");
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_QUEUE_ERROR);
            }
        }catch (Exception e){
            logger.error("[async->cluster] create blockchain's cluster error，make sure namespace name、pv、pvc、Orderer org、Orderer node、ca server are success to create");
            throw new K8SException(BlockChainResultCode.CREATE_CLUSTER_ERROR);
        }
    }
}
