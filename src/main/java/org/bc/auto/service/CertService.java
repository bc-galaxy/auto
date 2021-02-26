package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCCert;

import java.util.List;

public interface CertService {

    int insertBCCert(BCCert bcCert)throws BaseRuntimeException;

    List<BCCert> getBCCertByOrgAdmin(String orgId)throws BaseRuntimeException;
}
