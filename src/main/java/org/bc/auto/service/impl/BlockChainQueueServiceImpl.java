package org.bc.auto.service.impl;

import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.listener.BlockChainEven;
import org.bc.auto.listener.BlockChainNetworkClusterListener;
import org.bc.auto.model.entity.*;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.HyperledgerFabricComponentsStartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlockChainQueueServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainQueueServiceImpl.class);

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterMapper(BCClusterMapper bcClusterMapper) {
        this.bcClusterMapper = bcClusterMapper;
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
                    //如果成功申请则进行状态修改，并入库。
                    //这里需要进行事务操作，现在的做法是详细的记录日志；并在失败的时候可以手动插入数据库。
                    //因为shell执行成功之后，是没有办法回滚操作的，即非原子性操作，必须达到最终一致性。采用手动补偿的方式。
                    //正式生产环境中可以采用消息队列，针对消息队列的消息进行最后确认以及补偿。
                    //当把证书添加至数据库中，意味着组织创建成功、并且完成系统通道中的添加。

                    break;
                }
                case "BCNode" : {
                    logger.info("[queue->org] 执行创建节点脚本");
                    BCNode bcNode = (BCNode) blockChainNetwork;
                    BCCluster bcCluster = bcClusterMapper.getClusterById(bcNode.getClusterId());
                    HyperledgerFabricComponentsStartUtils.generateNodeCerts(bcCluster,bcNode);
                    //添加节点操作，成功生成节点所需要的证书文件，并入库。

                    //并且发布监听的事件，此事件是通知节点启动K8S的pod
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
