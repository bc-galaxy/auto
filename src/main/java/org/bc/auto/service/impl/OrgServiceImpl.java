package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.dao.BCOrgMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.source.BlockChainFabricNodeEventSource;
import org.bc.auto.listener.source.BlockChainFabricOrgEventSource;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.service.NodeService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrgServiceImpl implements OrgService {
    private static final Logger logger = LoggerFactory.getLogger(OrgService.class);

    private BCOrgMapper bcOrgMapper;
    @Autowired
    public void setBcOrgMapper(BCOrgMapper bcOrgMapper) {
        this.bcOrgMapper = bcOrgMapper;
    }

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterMapper(BCClusterMapper bcClusterMapper) {
        this.bcClusterMapper = bcClusterMapper;
    }

    private NodeService nodeService;
    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @Transactional
    public BCOrg createOrg(JSONObject jsonObject)throws BaseRuntimeException {

        //获取集群编号用来确认在哪个集群中创建组织
        String clusterId = jsonObject.getString("clusterId");
        ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
        logger.debug("[org->create] create blockchain's org, get the cluster id is :{}",clusterId);

        //获取集群对象，以获取更多的集群信息
        //并对返回的结果进行判断
        BCCluster bcCluster =bcClusterMapper.getClusterById(clusterId);
        String clusterName = bcCluster.getClusterName();
        ValidatorUtils.isNotNull(clusterName, ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NULL);
        logger.debug("[org->create] create blockchain's org, from database the cluster name is :{}",clusterName);

        //获取需要创建的组织名称
        String orgName = jsonObject.getString("orgName");
        ValidatorUtils.isNotNull(orgName, ValidatorResultCode.VALIDATOR_ORG_NAME_NULL);
        logger.debug("[org->create] create blockchain's org, get the org name is :{}",orgName);
        //判断组织名称是否匹配
        if(!ValidatorUtils.isMatches(orgName,ValidatorUtils.FABRIC_ORG_NAME_REGEX)){
            logger.error("[org->create] create blockchain's org, org name error，make sure org name '{}' accord with naming convention",orgName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_NAME_NOT_MATCH);
        }

        //检查组织是否存在
        List<BCOrg> bcOrgList = bcOrgMapper.getOrgByOrgNameAndClusterId(orgName,clusterId);
        if(!ValidatorUtils.isNull(bcOrgList)){
            logger.warn("[org->create] create blockchain's org，make sure org name '{}' is not exists",orgName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_NAME_RE);
        }

        //自动拼接组织的Msp
        String orgMspId = String.join("",orgName,"MSP");
        logger.debug("[org->create] create blockchain's org, get the org msp id is :{}",orgMspId);
        int orgIsTls = jsonObject.getIntValue("orgIsTls");
        int orgType = jsonObject.getIntValue("orgType");

        logger.info("[org->create] create blockchain's org, get the org name is :{}, org's msp id is :{},cluster id is :{}, cluster name is :{}",
                orgName, orgMspId, clusterId, clusterName);

        BCOrg bcOrg = new BCOrg();
        bcOrg.setId(StringUtils.getId());
        bcOrg.setClusterId(clusterId);
        bcOrg.setClusterName(clusterName);
        bcOrg.setOrgStatus(1);
        bcOrg.setOrgName(orgName);
        bcOrg.setOrgMspId(orgMspId);
        bcOrg.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
        bcOrg.setOrgIsTls(orgIsTls);
        bcOrg.setOrgType(orgType);
        int orgResult = bcOrgMapper.insertOrg(bcOrg);
        //如果组织成功入库
        if(!ValidatorUtils.isGreaterThanZero(orgResult)){
            logger.error("[org->create] create blockchain's org，insert database error.");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_INSERT_ERROR);
        }

        //把对应的组织对象添加至脚本的执行队列中，等待执行;使用异步的形式，减少前端的等待时间.
        //主要用于生成组织的MSP证书和TLS的证书
        //如果生产环境在集群、并发情况下，并且添加Peer组织无法控制；
        //  1. 可以按照集群的状态决定是否创建peer的组织(使用数据库的状态值，用CAS的方式控制)。
        //  2. 可以在redis/db设置独立的锁形式，防止并发争抢（auto集群的情况下，并发添加组织可能会造成影响）。
        //  3. 此队列替换为消息队列控制，单次消费一条消息。
        //  4. 改变添加方式用定时任务的形式对库里的数据进行调度处理。
        BlockChainFabricOrgEventSource blockChainFabricOrgEventSource = new BlockChainFabricOrgEventSource();
        blockChainFabricOrgEventSource.setBcOrg(bcOrg);
        boolean flag = BlockChainShellQueueUtils.add(blockChainFabricOrgEventSource);
        if(!flag){
            logger.error("[org->create] create blockchain's org，join task queue error. check error and retry");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
        }
        return bcOrg;
    }

    public BCOrg getOrgByOrgId(String orgId)throws BaseRuntimeException{
        return bcOrgMapper.getOrgByOrgId(orgId);
    }
}
