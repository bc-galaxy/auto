package org.bc.auto.model.entity;

import java.util.ArrayList;
import java.util.List;

public class BlockChainArrayList<E> implements BlockChainNetwork {

    private List<E> eList;

    public List<E> geteList() {
        return eList;
    }

    public void seteList(List<E> eList) {
        this.eList = eList;
    }
}
