package org.bc.auto.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.listener.source.BlockChainFabricNodeEventSource;

import java.util.List;

public interface NodeService {

    boolean createNode(JSONArray jsonArray)throws BaseRuntimeException;

    int updateNode(BCNode bcNode)throws BaseRuntimeException;

    List<BCNode> getNodeByNodeTypeAndCluster(int nodeType, String clusterId);

    boolean joinChannel(JSONObject jsonObject) throws BaseRuntimeException;
}
