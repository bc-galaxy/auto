package org.bc.auto.utils;

import org.bc.auto.model.entity.BCCluster;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainShellQueueUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainShellQueueUtilsTest.class);

    @Test
    public void testAdd(){
        for(int i=0;i<10;i++){
            BCCluster bcCluster = new BCCluster();
            //设置uuid的主键
            bcCluster.setId(StringUtils.getId());
            //设置网络的集群名称
            bcCluster.setClusterName("clusterName");
            //设置此网络集群的版本信息
            bcCluster.setClusterVersion("clusterVersion");
            //设置网络集群的添加时间
            bcCluster.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
            //设置安装的启动状态
            bcCluster.setInstallStatus(1);
            //设置集群的过期时间，如果为0则永远不过期；如果设置了日期则会定时删除此网络集群
            bcCluster.setExpiresTime(0L);
//            BlockChainShellQueueUtils.add(bcCluster);
            logger.info("队列的长度为：{}", BlockChainShellQueueUtils.size());
        }
    }

    @Test
    public void testRemove()throws Exception{
        for(int i=0;i<10;i++){
            BCCluster bcCluster = new BCCluster();
            //设置uuid的主键
            bcCluster.setId(StringUtils.getId());
            //设置网络的集群名称
            bcCluster.setClusterName("clusterName"+i);
            //设置此网络集群的版本信息
            bcCluster.setClusterVersion("clusterVersion");
            //设置网络集群的添加时间
            bcCluster.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
            //设置安装的启动状态
            bcCluster.setInstallStatus(1);
            //设置集群的过期时间，如果为0则永远不过期；如果设置了日期则会定时删除此网络集群
            bcCluster.setExpiresTime(0L);
//            BlockChainShellQueueUtils.add(bcCluster);
        }
        BCCluster bcCluster = (BCCluster) BlockChainShellQueueUtils.peek();
        logger.info("队列的元素为：{}",bcCluster.getClusterName());
        bcCluster = (BCCluster) BlockChainShellQueueUtils.peek();
        logger.info("队列的第一个元素为：{}",bcCluster.getClusterName());


    }

    @Test
    public void testQueueWait()throws Exception{

        logger.info("线程名称:{}",Thread.currentThread().getName());

        for(int i=0;i<10;i++){
            ThreadPoolManager.newInstance().addExecuteTask(new Runnable() {
                @Override
                public void run() {
                    try{
                        logger.info("获取任务队列长度{}", BlockChainShellQueueUtils.size());

                        BCCluster bcCluster = new BCCluster();
                        bcCluster.setId(StringUtils.getId());
                        bcCluster.setClusterName("clusterName"+Thread.currentThread().getName());
                        bcCluster.setClusterVersion("clusterVersion");
                        bcCluster.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
                        bcCluster.setInstallStatus(1);
                        bcCluster.setExpiresTime(0L);
//                        BlockChainShellQueueUtils.add(bcCluster);

                    }catch (Exception e){
                        logger.error("获取任务队列失败");
                    }
                }
            });
        }

        while (true){
            logger.info("调用次数");
            BCCluster bcCluster = (BCCluster) BlockChainShellQueueUtils.peek();
//            logger.info("队列出口元素：{};线程名称:{}",bcCluster.getClusterName(),Thread.currentThread().getName());

        }

    }

}
