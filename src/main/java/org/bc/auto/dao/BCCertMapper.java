package org.bc.auto.dao;

import org.apache.ibatis.annotations.Param;
import org.bc.auto.model.entity.BCCert;
import org.bc.auto.model.entity.BCClusterInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCCertMapper {

    int insertBCCert(BCCert bcCert);

    List<BCCert> getBCCertByOrgAdmin(@Param(value = "orgId") String orgId);

}
