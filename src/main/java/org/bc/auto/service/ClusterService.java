package org.bc.auto.service;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.model.entity.BCCluster;

import java.util.List;

public interface ClusterService {

    void createCluster(JSONObject jsonObject)throws BaseRuntimeException;

    List<BCCluster> getBCClusterList()throws BaseRuntimeException;

}
