package org.bc.auto.listener.source;

import com.alibaba.fastjson.JSONArray;

public class BlockChainFabricJoinChannelEventSource implements BlockChainEventSource {

    private JSONArray jsonArray;

    public JSONArray getJsonArray() {
        return jsonArray;
    }

    public void setJsonArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }
}
