package org.bc.auto.listener.source;

import org.bc.auto.listener.source.BlockChainEventSource;

import java.util.List;

public class BlockChainFabricNodeEventSource<E> implements BlockChainEventSource {

    private List<E> eList;

    public List<E> geteList() {
        return eList;
    }

    public void seteList(List<E> eList) {
        this.eList = eList;
    }
}
