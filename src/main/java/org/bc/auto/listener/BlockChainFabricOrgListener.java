package org.bc.auto.listener;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.exception.K8SException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.source.BlockChainFabricOrgEventSource;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.HyperledgerFabricComponentsStartUtils;
import org.bc.auto.utils.SpringBeanUtil;
import org.bc.auto.utils.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainFabricOrgListener implements BlockChainListener{
    private static final Logger logger = LoggerFactory.getLogger(BlockChainFabricOrgListener.class);

    @Override
    public void doEven(BlockChainEvent blockChainEvent) {

        try{
//                    OrgService orgService = SpringBeanUtil.getBean(OrgService.class);
            BCClusterMapper bcClusterMapper = SpringBeanUtil.getBean(BCClusterMapper.class);
            //获取需要创建的组织对象
            BlockChainFabricOrgEventSource blockChainFabricOrgEventSource = (BlockChainFabricOrgEventSource)blockChainEvent.getBlockChainEventSource();
            BCOrg bcOrg = blockChainFabricOrgEventSource.getBcOrg();

            BCCluster bcCluster = bcClusterMapper.getClusterById(bcOrg.getClusterId());
            //这里的组织处理专门针对Orderer的组织
            //添加成功之后，如果是Orderer的组织类型，则进行orderer节点创建。
            //否则就不进行创建，创建的节点个数由集群传入的值为准。
            //并且需要做orderer组织的判断，同一个集群/nameSpace中仅仅只有一个orderer组织。
            if(bcOrg.getOrgType() == 1){
                //先进行文件的创建，此操作是特殊操作，在第一次启动集群的时候需要执行。
                //执行创建之前，需要orderer节点相关的证书。
                HyperledgerFabricComponentsStartUtils.buildFabricChain(bcCluster,bcOrg);

                for(int i=0;i<bcCluster.getOrdererCount();i++){
                    JSONObject jsonObjectOrderer = new JSONObject();
                    jsonObjectOrderer.put("clusterId",bcCluster.getId());
                    jsonObjectOrderer.put("nodeName","orderer"+i);
                    jsonObjectOrderer.put("nodeType",1);
                    jsonObjectOrderer.put("orgId",bcOrg.getId());

//                            nodeService.createNode(jsonObjectOrderer);
                }
                //当节点创建完成之后，开始生成config的tx文件
            }

            //把对应的组织对象添加至脚本的执行队列中，等待执行
            boolean flag = BlockChainShellQueueUtils.add(blockChainFabricOrgEventSource);
            if(!flag){
                logger.error("[async] 组织加入任务队列错误，请确认错误信息。");
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
            }
        }catch (Exception e){
            logger.error("[async] create cluster error, error info is {}",e.getMessage());
            throw new K8SException();
        }

    }
}
