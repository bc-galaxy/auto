package org.bc.auto.utils;

import org.bc.auto.listener.source.BlockChainEventSource;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockChainShellQueueUtils {

    private static final BlockingQueue<BlockChainEventSource> BLOCK_CHAIN_SHELL_QUEUE = new ArrayBlockingQueue(64);

    public static boolean add(BlockChainEventSource blockChainNetwork){
        boolean flag = BLOCK_CHAIN_SHELL_QUEUE.offer(blockChainNetwork);

        return flag;
    }

    /**
     * 直接选择'移除'操作来获取元素，
     * 防止由于程序异常、数据异常等原因，造成的队列一直卡在队列的第一个元素之上。
     * 执行该方法可以快速进行后面的元素进行操作。
     *
     * 选择队列等待来减少cpu的消耗。
     *
     * 程序启动的时候，就开始异步线程执行队列的操作。
     *
     * @return
     * @throws Exception
     */
    public static BlockChainEventSource peek()throws InterruptedException{
        return BLOCK_CHAIN_SHELL_QUEUE.take();
    }

    public static int size(){
        return BLOCK_CHAIN_SHELL_QUEUE.size();
    }

    public static String getElementClassName(BlockChainEventSource blockChainNetwork){
        String className = blockChainNetwork.getClass().getName();
        int beginIndex = className.lastIndexOf(".");
        beginIndex = beginIndex+1;
        className = className.subSequence(beginIndex,className.length()).toString();
        return className;
    }

    public static Queue get()throws Exception{
        return BLOCK_CHAIN_SHELL_QUEUE;
    }

}
