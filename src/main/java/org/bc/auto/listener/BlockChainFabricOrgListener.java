package org.bc.auto.listener;

import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.exception.K8SException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class BlockChainFabricOrgListener implements BlockChainListener{
    private static final Logger logger = LoggerFactory.getLogger(BlockChainFabricOrgListener.class);

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
                    //获取需要创建的组织对象
                    BCOrg bcOrg = (BCOrg)blockChainEven.getBlockChainNetwork();
                    //把对应的组织对象添加至脚本的执行队列中，等待执行
                    boolean flag = BlockChainShellQueueUtils.add(bcOrg);
                    if(!flag){
                        logger.error("[async] 组织加入任务队列错误，请确认错误信息。");
                        throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
                    }
                }catch (Exception e){
                    logger.error("[async] create cluster error, error info is {}",e.getMessage());
                    throw new K8SException();
                }
            }

        });
    }
}
