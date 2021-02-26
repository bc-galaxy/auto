package org.bc.auto.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;

public interface NodeService {

    void createNode(JSONArray jsonArray)throws BaseRuntimeException;
}
