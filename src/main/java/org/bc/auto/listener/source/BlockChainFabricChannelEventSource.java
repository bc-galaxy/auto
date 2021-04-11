package org.bc.auto.listener.source;

import org.bc.auto.model.entity.BCChannel;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;

import java.util.List;

public class BlockChainFabricChannelEventSource implements BlockChainEventSource{

    private BCChannel bcChannel;

    private BCCluster bcCluster;

    private List<BCOrg> bcOrgs;

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

    public List<BCOrg> getBcOrgs() {
        return bcOrgs;
    }

    public void setBcOrgs(List<BCOrg> bcOrgs) {
        this.bcOrgs = bcOrgs;
    }
}
