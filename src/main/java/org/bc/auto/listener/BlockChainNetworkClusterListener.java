package org.bc.auto.listener;

import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BlockChainNetwork;
import org.bc.auto.service.ClusterService;
import org.bc.auto.utils.DateUtils;
import org.bc.auto.utils.K8SUtils;
import org.bc.auto.utils.ShellUtils;
import org.bc.auto.utils.ThreadPoolManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                    BCCluster bcCluster = (BCCluster)blockChainEven.getBlockChainNetwork();
                    logger.info("[async] create k8s name space, name is {}",bcCluster.getClusterName());
                    K8SUtils.createNamespace(bcCluster.getClusterName());
                }catch (Exception e){
                    System.out.println("exception thread");
                }
            }

        });
    }
}
