package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.listener.BlockChainEven;
import org.bc.auto.listener.BlockChainFabricNodeListener;
import org.bc.auto.model.entity.*;
import org.bc.auto.service.BlockChainQueueService;
import org.bc.auto.service.CertService;
import org.bc.auto.service.NodeService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.HyperledgerFabricComponentsStartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockChainQueueServiceImpl implements BlockChainQueueService {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainQueueServiceImpl.class);

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterMapper(BCClusterMapper bcClusterMapper) {
        this.bcClusterMapper = bcClusterMapper;
    }

    private CertService certService;
    @Autowired
    public void setCertService(CertService certService) {
        this.certService = certService;
    }

    private NodeService nodeService;
    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private OrgService orgService;
    @Autowired
    public void setOrgService(OrgService orgService) {
        this.orgService = orgService;
    }

    public void run(){
        while(true){
            BlockChainNetwork blockChainNetwork = null;
            try{
                blockChainNetwork = BlockChainShellQueueUtils.peek();
            }catch (InterruptedException e){
                logger.info("[queue->exception] 队列获取元素异常，程序可能已经崩溃。异常信息:{}",e.getMessage());
            }

            String className = BlockChainShellQueueUtils.getElementClassName(blockChainNetwork);
            switch (className){
                //如果是组织的类型，进行组织的脚本执行
                case "BCOrg" : {
                    logger.info("[queue->org] 执行创建组织脚本");
                    //获取集群对象，以获取更多的集群信息
                    //并对返回的结果进行判断
                    BCOrg bcOrg = (BCOrg) blockChainNetwork;
                    BCCluster bcCluster = bcClusterMapper.getClusterById(bcOrg.getClusterId());
                    BCCert bcCert = HyperledgerFabricComponentsStartUtils.generateOrgCerts(bcCluster, bcOrg);
                    //如果成功申请则进行组织的状态修改，并入库。
                    //这里需要进行事务操作：
                    //  shell执行成功之后，是没有办法回滚操作的，即非原子性操作，必须达到最终一致性。可以采用补偿的方式。
                    //  正式生产环境中可以采用消息队列，针对消息队列的消息进行最后确认以及补偿。
                    //  当把证书添加至数据库中，意味着组织创建成功。
                    certService.insertBCCert(bcCert);
                    break;
                }
                case "BlockChainNodeList" : {
                    logger.info("[queue->node] 执行创建节点脚本");

                    BlockChainNodeList<BCNode> bcNodeBlockChainArrayList = (BlockChainNodeList<BCNode>) blockChainNetwork;
                    List<BCNode> bcNodeList = bcNodeBlockChainArrayList.geteList();
                    //为节点申请节点证书
                    for(int i=0;i<bcNodeList.size();i++){
                        BCNode bcNode = bcNodeList.get(i);
                        //节点开始的时候生成证书
                        BCCluster bcCluster = bcClusterMapper.getClusterById(bcNode.getClusterId());
                        HyperledgerFabricComponentsStartUtils.generateNodeCerts(bcCluster,bcNode);
                    }

                    //监听节点事件，如果是orderer节点的情况下。
                    //需要创建创世区块等文件
                    if(bcNodeList.get(0).getNodeType() == 1){
                        BCCluster bcCluster = bcClusterMapper.getClusterById(bcNodeList.get(0).getClusterId());
                        BCOrg bcOrg =orgService.getOrgByOrgId(bcNodeList.get(0).getOrgId());
                        HyperledgerFabricComponentsStartUtils.buildFabricChain(bcCluster,bcOrg);
                    }

                    //启动节点
                    for(int i=0;i<bcNodeList.size();i++){
                        BCNode bcNode = bcNodeList.get(i);
                        //通知K8S启动对应的pod节点,发布监听
                        new BlockChainEven(new BlockChainFabricNodeListener(),bcNode).doEven();
                    }

                    break;
                }
                case "BCCert" :
                    logger.info("[queue->cert] 执行创建节点脚本");
                    break;
                default:

            }
        }
    }

}
