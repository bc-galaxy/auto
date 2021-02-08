package org.bc.auto.service.impl;

import org.bc.auto.model.entity.BlockChainNetwork;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.HyperledgerFabricComponentsStartUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BlockChainQueueServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainQueueServiceImpl.class);

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
                case "BCOrg" :
                    logger.info("[queue->org] 执行创建组织脚本");

                    HyperledgerFabricComponentsStartUtils.generateOrgCerts(null,null,null);
                    //申请Ca证书,注册组织
                    break;
                case "BCNode" :
                    logger.info("[queue->org] 执行创建节点脚本");
                    break;
                case "BCCert" :
                    logger.info("[queue->cert] 执行创建节点脚本");
                    break;
                default:

            }
        }
    }

}
