package org.bc.auto.listener;

import org.bc.auto.model.entity.BlockChainNetwork;

public class BlockChainEven {

    private BlockChainNetwork blockChainNetwork;

    private BlockChainListener blockChainListener;

    public BlockChainEven(){
        super();
    }

    public BlockChainEven(BlockChainListener blockChainListener,BlockChainNetwork blockChainNetwork){
        this.blockChainNetwork = blockChainNetwork;
        this.blockChainListener = blockChainListener;
    }

    public BlockChainNetwork getBlockChainNetwork() {
        return blockChainNetwork;
    }

    public void createK8SCluster() {
        this.blockChainListener.doEven(this);
    }
}
