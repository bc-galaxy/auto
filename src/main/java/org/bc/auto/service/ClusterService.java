package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCClusterInfo;

import java.util.List;

public interface ClusterService {

    BCCluster createCluster(JSONObject jsonObject)throws BaseRuntimeException;

    List<BCCluster> getBCClusterList()throws BaseRuntimeException;

    BCClusterInfo getBCClusterInfo(String clusterId)throws BaseRuntimeException;

    BCCluster getBCCluster(String clusterId)throws BaseRuntimeException;
}
