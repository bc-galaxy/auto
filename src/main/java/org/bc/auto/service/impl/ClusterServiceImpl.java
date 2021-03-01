package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCClusterInfoMapper;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.BlockChainEven;
import org.bc.auto.listener.BlockChainNetworkClusterListener;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCClusterInfo;
import org.bc.auto.service.ClusterService;
import org.bc.auto.utils.DateUtils;
import org.bc.auto.utils.StringUtils;
import org.bc.auto.utils.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClusterServiceImpl implements ClusterService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterService.class);

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterDao(BCClusterMapper bcClusterMapper){
        this.bcClusterMapper = bcClusterMapper;
    }

    private BCClusterInfoMapper bcClusterInfoMapper;
    @Autowired
    public void setBcClusterInfoMapper(BCClusterInfoMapper bcClusterInfoMapper) {
        this.bcClusterInfoMapper = bcClusterInfoMapper;
    }

    @Override
    @Transactional
    public boolean createCluster(JSONObject jsonObject) throws BaseRuntimeException {
        //创建返回的结果变量
        boolean resultFlag = true;

        //检查集群名称是否为空
        String clusterName = jsonObject.getString("clusterName");
        ValidatorUtils.isNotNull(clusterName, ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NULL);
        if(!ValidatorUtils.isMatches(clusterName,ValidatorUtils.FABRIC_CLUSTER_NAME_REGEX)){
            logger.error("[cluster->create] 创建区块链集群，集群名'{}'不符合规则",clusterName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NOT_MATCH);
        }
        logger.debug("[cluster->create] 创建区块链集群，获取的集群信息为:{}",clusterName);
        //根据获取到的集群检查集群名称是否重复
        List<BCCluster> clusterList = bcClusterMapper.getClusterByClusterName(clusterName);
        //假如根据cluster的名字获取到的对象不为空，则证明重复
        if(!ValidatorUtils.isNull(clusterList)){
            logger.error("[cluster->create] 创建区块链集群，集群名'{}'已存在",clusterName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CLUSTER_NAME_RE);
        }
        //检查安装的集群版本是否为空
        String clusterVersion = jsonObject.getString("clusterVersion");
        ValidatorUtils.isNotNull(clusterVersion, ValidatorResultCode.VALIDATOR_CLUSTER_VERSION_NULL);
        logger.debug("[cluster->create] 创建区块链集群，获取的版本信息为:{}",clusterVersion);
        //检查安装的集群类型是否为空，1：Fabric，2：QuoRom
        int clusterType = jsonObject.getIntValue("clusterType");
        logger.debug("[cluster->create] 创建区块链集群，获取的安装类型信息为:{}",clusterType);
        if(!ValidatorUtils.isGreaterThanZero(clusterType)){
            logger.error("[cluster->create] 创建区块链集群，请检查安装类型信息:{}",clusterType);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CLUSTER_TYPE_NULL);
        }

        //创建区块链网络的集群对象
        BCCluster bcCluster = new BCCluster();
        //设置uuid的主键
        bcCluster.setId(StringUtils.getId());
        //设置网络的集群名称
        bcCluster.setClusterName(clusterName);
        //设置此网络集群的版本信息
        bcCluster.setClusterVersion(clusterVersion);
        //设置集群的共识类型
        bcCluster.setClusterConsensusType(1);
        //设置orderer节点的总数
        bcCluster.setOrdererCount(3);
        //设置网络集群的添加时间
        bcCluster.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
        //设置安装的启动状态
        bcCluster.setInstallStatus(1);
        //设置集群的过期时间，如果为0则永远不过期；如果设置了日期则会定时删除此网络集群
        bcCluster.setExpiresTime(0L);
        //设置集群的世界状态存储类型
        bcCluster.setStateDbType(1);
        int clusterResult = bcClusterMapper.insertCluster(bcCluster);
        logger.info("[cluster->create] 创建区块链集群，集群信息如下：集群名称:{}，集群版本:{}，集群中orderer节点总数:{}",
                bcCluster.getClusterName(),bcCluster.getClusterVersion(),bcCluster.getOrdererCount());
        //如果集群成功入库
        if(!ValidatorUtils.isGreaterThanZero(clusterResult)){
            logger.error("[cluster->create] 创建区块链集群，插入数据库失败，请确认。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CLUSTER_INSERT_ERROR);
        }

        //触发监听事件，去创建集群
        new BlockChainEven(new BlockChainNetworkClusterListener(),bcCluster).doEven();
        //返回结果集
        return resultFlag;
    }

    public BCCluster getBCCluster(String clusterId)throws BaseRuntimeException{
        BCCluster  bcCluster = bcClusterMapper.getClusterById(clusterId);

        if(ValidatorUtils.isNotNull(bcCluster) ){
            return bcCluster;
        }

        throw new ValidatorException();
    }

    public List<BCCluster> getBCClusterList()throws BaseRuntimeException{
        return bcClusterMapper.getAllCluster();
    }

    public BCClusterInfo getBCClusterInfo(String clusterId)throws BaseRuntimeException{
        List<BCClusterInfo>  bcClusterInfoList = bcClusterInfoMapper.getBCClusterInfoByClusterId(clusterId);

        if(ValidatorUtils.isNotNull(bcClusterInfoList) && bcClusterInfoList.size() == 1){
            return bcClusterInfoList.get(0);
        }

        throw new ValidatorException();
    }
}
