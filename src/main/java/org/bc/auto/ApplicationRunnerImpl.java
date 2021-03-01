package org.bc.auto;

import org.bc.auto.service.BlockChainQueueService;
import org.bc.auto.utils.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ApplicationRunnerImpl implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRunnerImpl.class);

    private BlockChainQueueService blockChainQueueService;
    @Autowired
    public void setBlockChainQueueService(BlockChainQueueService blockChainQueueService) {
        this.blockChainQueueService = blockChainQueueService;
    }

    public void run(ApplicationArguments args) throws Exception {
        logger.info("[application->init]服务启动完成，开始加载线程执行监听脚本队列");
        ThreadPoolManager.newInstance().addExecuteTask(new Runnable(){
            public void run(){
                blockChainQueueService.run();
            }
        });
        logger.info("[application->init]服务启动完成，完成加载线程执行监听脚本队列");
    }
}
