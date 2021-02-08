package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;

public interface NodeService {

    void createNode(JSONObject jsonObject)throws BaseRuntimeException
}
