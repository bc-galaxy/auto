package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;

public interface OrgService {

    void createOrg(JSONObject jsonObject)throws BaseRuntimeException;
}
