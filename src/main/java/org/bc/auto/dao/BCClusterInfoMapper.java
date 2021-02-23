package org.bc.auto.dao;

import org.apache.ibatis.annotations.Param;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCClusterInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCClusterInfoMapper {

    int insertBCClusterInfo(BCClusterInfo bcClusterInfo);

    List<BCClusterInfo> getBCClusterInfoByClusterId(@Param(value = "clusterId") String clusterId);
}
