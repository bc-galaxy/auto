package org.bc.auto.listener;

import org.bc.auto.listener.source.BlockChainEventSource;

public class BlockChainEvent {

    private BlockChainEventSource blockChainEventSource;

    private BlockChainListener blockChainListener;

    public BlockChainEvent(){
        super();
    }

    public BlockChainEvent(BlockChainListener blockChainListener, BlockChainEventSource blockChainEventSource){
        this.blockChainEventSource = blockChainEventSource;
        this.blockChainListener = blockChainListener;
    }

    public BlockChainEventSource getBlockChainEventSource() {
        return blockChainEventSource;
    }

    public void doEven() {
        this.blockChainListener.doEven(this);
    }
}
