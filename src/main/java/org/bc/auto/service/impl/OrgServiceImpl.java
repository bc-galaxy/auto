package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.dao.BCOrgMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.service.NodeService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public BCOrg createOrg(JSONObject jsonObject)throws BaseRuntimeException {

        String clusterId = jsonObject.getString("clusterId");
        ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
        logger.debug("[org->create] 创建组织，获取的集群编号信息为:{}",clusterId);

        //获取集群对象，以获取更多的集群信息
        //并对返回的结果进行判断
        BCCluster bcCluster =bcClusterMapper.getClusterById(clusterId);

        String clusterName = bcCluster.getClusterName();
        ValidatorUtils.isNotNull(clusterName, ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NULL);
        logger.info("[org->create] 创建组织，获取的集群名称信息为:{}",clusterName);

        String orgName = jsonObject.getString("orgName");
        ValidatorUtils.isNotNull(orgName, ValidatorResultCode.VALIDATOR_ORG_NAME_NULL);
        //判断组织名称是否匹配
        if(!ValidatorUtils.isMatches(orgName,ValidatorUtils.FABRIC_ORG_NAME_REGEX)){
            logger.error("[org->create] 创建组织，组织名称错误，获取到的组织名称为:{}",orgName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_NAME_NOT_MATCH);
        }
        logger.info("[org->create] 创建组织，获取的组织名称信息为:{}",orgName);
        //检查组织是否存在
        List<BCOrg> bcOrgList = bcOrgMapper.getOrgByOrgNameAndClusterId(orgName,clusterId);
        if(!ValidatorUtils.isNull(bcOrgList)){
            logger.warn("[org->create] 创建区块链集群，组织名'{}'已存在",orgName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_NAME_RE);
        }

        //自动拼接组织的Msp
        String orgMspId = String.join("",orgName,"MSP");

        int orgIsTls = jsonObject.getIntValue("orgIsTls");
        int orgType = jsonObject.getIntValue("orgType");

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
            logger.error("[org->create] 创建组织，插入数据库失败，请确认。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_INSERT_ERROR);
        }

        //把对应的组织对象添加至脚本的执行队列中，等待执行
        //主要用于生成Orderer组织的MSP证书和TLS的证书
        //在同一个集群中，创建Orderer组织的脚本得确保是优先执行；理论上队列的特性只要确定该任务是先加入队列即可。
        //如果生产环境在集群、并发情况下，并且添加Peer组织无法控制；可以按照集群的状态决定是否创建peer的组织。
        boolean flag = BlockChainShellQueueUtils.add(bcOrg);
        if(!flag){
            logger.error("[async] 组织加入任务队列错误，请确认错误信息。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
        }
        return bcOrg;
    }

    public BCOrg getOrgByOrgId(String orgId)throws BaseRuntimeException{
        return bcOrgMapper.getOrgByOrgId(orgId);
    }
}
