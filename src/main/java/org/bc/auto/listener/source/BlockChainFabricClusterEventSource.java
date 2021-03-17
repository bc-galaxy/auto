package org.bc.auto.listener.source;

import org.bc.auto.model.entity.BCCluster;

public class BlockChainFabricClusterEventSource implements BlockChainEventSource{

    private BCCluster bcCluster;

    public BCCluster getBcCluster() {
        return bcCluster;
    }

    public void setBcCluster(BCCluster bcCluster) {
        this.bcCluster = bcCluster;
    }
}
