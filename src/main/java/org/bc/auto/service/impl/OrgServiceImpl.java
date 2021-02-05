package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCOrgMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.BlockChainQueueUtils;
import org.bc.auto.utils.DateUtils;
import org.bc.auto.utils.StringUtils;
import org.bc.auto.utils.ValidatorUtils;
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

    public void createOrg(JSONObject jsonObject)throws BaseRuntimeException {

        String clusterId = jsonObject.getString("clusterId");
        ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
        logger.debug("[org->create] 创建组织，获取的集群编号信息为:{}",clusterId);

        String orgName = jsonObject.getString("orgName");
        ValidatorUtils.isNotNull(orgName, ValidatorResultCode.VALIDATOR_ORG_NAME_NULL);
        //判断组织名称是否匹配
        if(!ValidatorUtils.isMatches(orgName,ValidatorUtils.FABRIC_ORG_NAME_REGEX)){
            logger.error("[org->create] 创建组织，组织名称错误，获取到的组织名称为:{}",orgName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_NAME_NOT_MATCH);
        }
        logger.info("[org->create] 创建组织，获取的组织名称信息为:{}",orgName);
        //检查组织是否存在
        List<BCOrg> bcOrgList = bcOrgMapper.getOrgByOrgNameAndCluster(orgName,clusterId);
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
        bcOrg.setOrgStatus(3);
        bcOrg.setOrgName(orgName);
        bcOrg.setOrgMspId(orgMspId);
        bcOrg.setCreateTime(DateUtils.getCurrentMillisTimeStamp());
        bcOrg.setOrgIsTls(orgIsTls);
        bcOrg.setOrgType(orgType);
        int orgResult = bcOrgMapper.insertOrg(bcOrg);
        //如果集群成功入库
        if(!ValidatorUtils.isGreaterThanZero(orgResult)){
            logger.error("[org->create] 创建组织，插入数据库失败，请确认。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_INSERT_ERROR);
        }
        boolean flag = BlockChainQueueUtils.add(bcOrg);
        if(!flag){
            logger.error("[org->create] 组织加入任务队列错误，请确认错误信息。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
        }
    }
}
