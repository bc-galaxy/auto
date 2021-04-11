package org.bc.auto.listener.source;

import com.alibaba.fastjson.JSONArray;
import org.bc.auto.model.entity.BCChannelOrgPeer;

import java.util.List;

public class BlockChainFabricJoinChannelEventSource implements BlockChainEventSource {

    private JSONArray jsonArray;

    private List<BCChannelOrgPeer> bcChannelOrgPeerList;

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public List<BCChannelOrgPeer> getBcChannelOrgPeerList() {
        return bcChannelOrgPeerList;
    }

    public void setBcChannelOrgPeerList(List<BCChannelOrgPeer> bcChannelOrgPeerList) {
        this.bcChannelOrgPeerList = bcChannelOrgPeerList;
    }
}
