package org.bc.auto.listener.source;

import org.bc.auto.model.entity.BCOrg;

public class BlockChainFabricOrgEventSource implements BlockChainEventSource{

    private BCOrg bcOrg;

    public BCOrg getBcOrg() {
        return bcOrg;
    }

    public void setBcOrg(BCOrg bcOrg) {
        this.bcOrg = bcOrg;
    }
}
