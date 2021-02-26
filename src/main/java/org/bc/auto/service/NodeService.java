package org.bc.auto.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCNode;

public interface NodeService {

    void createNode(JSONArray jsonArray)throws BaseRuntimeException;

    int updateNode(BCNode bcNode)throws BaseRuntimeException;
}
