package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCOrg;

public interface OrgService {

    BCOrg createOrg(JSONObject jsonObject)throws BaseRuntimeException;
    BCOrg getOrgByOrgId(String orgId)throws BaseRuntimeException;
}
