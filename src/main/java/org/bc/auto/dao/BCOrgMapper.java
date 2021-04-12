package org.bc.auto.dao;

import org.apache.ibatis.annotations.Param;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCOrgMapper {

    int insertOrg(BCOrg bcOrg);

    List<BCOrg> getAllOrg();
    BCOrg getOrgByOrgId(@Param(value = "orgId") String orgId);

    List<BCOrg>  getOrgByOrgIds(List<String> orgIds);

    List<BCOrg> getOrgByOrgName(@Param(value = "orgName") String orgName);
    List<BCOrg> getOrgByOrgNameAndClusterId(@Param(value = "orgName") String orgName, @Param(value = "clusterId") String clusterId);

}
