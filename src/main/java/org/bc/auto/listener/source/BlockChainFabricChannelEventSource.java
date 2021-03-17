package org.bc.auto.listener.source;

import org.bc.auto.model.entity.BCChannel;
import org.bc.auto.model.entity.BCCluster;

import java.util.List;

public class BlockChainFabricChannelEventSource implements BlockChainEventSource{

    private BCChannel bcChannel;

    private BCCluster bcCluster;

    private List<String> orgNames;

    public BCChannel getBcChannel() {
        return bcChannel;
    }

    public void setBcChannel(BCChannel bcChannel) {
        this.bcChannel = bcChannel;
    }

    public BCCluster getBcCluster() {
        return bcCluster;
    }

    public void setBcCluster(BCCluster bcCluster) {
        this.bcCluster = bcCluster;
    }

    public List<String> getOrgNames() {
        return orgNames;
    }

    public void setOrgNames(List<String> orgNames) {
        this.orgNames = orgNames;
    }
}
