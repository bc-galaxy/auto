package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.BlockChainEven;
import org.bc.auto.listener.BlockChainNetworkClusterListener;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.service.ClusterService;
import org.bc.auto.utils.DateUtils;
import org.bc.auto.utils.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClusterServiceImpl implements ClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterDao(BCClusterMapper bcClusterMapper){
        this.bcClusterMapper = bcClusterMapper;
    }


    @Override
    public void createCluster(JSONObject jsonObject) throws BaseRuntimeException {
        //检查集群名称是否为空
        String clusterName = jsonObject.getString("clusterName");
        ValidatorUtils.isNotNull(clusterName, ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NULL);

        //检查安装的集群版本是否为空
        String clusterVersion = jsonObject.getString("clusterVersion");
        ValidatorUtils.isNotNull(clusterVersion, ValidatorResultCode.VALIDATOR_CLUSTER_VERSION_NULL);

        //检查安装的集群类型是否为空，1：Fabric，2：QuoRom
        int clusterType = jsonObject.getIntValue("clusterType");
        if(!ValidatorUtils.isGreaterThanZero(clusterType)){
            logger.error("传入的参数有误，请检查参数值：{}",clusterType);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CLUSTER_TYPE_NULL);
        }

        BCCluster bcCluster = new BCCluster();
        bcCluster.setId("bc-auto-cluster");
        bcCluster.setClusterName(clusterName);
        bcCluster.setClusterVersion(clusterVersion);
        bcCluster.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
        bcCluster.setInstallStatus(1);
        bcCluster.setExpiresTime(0L);
        int clusterResult = bcClusterMapper.insertCluster(bcCluster);

        if(!ValidatorUtils.isGreaterThanZero(clusterResult)){
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CLUSTER_INSERT_ERROR);
        }
        new BlockChainEven(new BlockChainNetworkClusterListener(),bcCluster).createK8SCluster();
    }

    public List<BCCluster> getBCClusterList()throws BaseRuntimeException{
        return bcClusterMapper.getAllCluster();
    }
}
