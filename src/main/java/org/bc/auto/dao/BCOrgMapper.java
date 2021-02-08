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

    List<BCOrg> getOrgByOrgName(@Param(value = "orgName") String orgName);
    List<BCOrg> getOrgByOrgNameAndCluster(@Param(value = "orgName") String orgName,@Param(value = "clusterId") String clusterId);

}