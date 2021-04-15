package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCChannelMapper;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.dao.BCOrgMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.listener.source.BlockChainFabricChannelEventSource;
import org.bc.auto.model.entity.BCChannel;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCOrg;
import org.bc.auto.service.ChannelService;
import org.bc.auto.service.OrgService;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.StringUtils;
import org.bc.auto.utils.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChannelServiceImpl implements ChannelService {
    private static final Logger logger = LoggerFactory.getLogger(ChannelService.class);

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterMapper(BCClusterMapper bcClusterMapper) {
        this.bcClusterMapper = bcClusterMapper;
    }

    private BCChannelMapper bcChannelMapper;
    @Autowired
    public void setBCChannelMapper(BCChannelMapper bcChannelMapper) {
        this.bcChannelMapper = bcChannelMapper;
    }

    private BCOrgMapper bcOrgMapper;
    @Autowired
    public void setBcOrgMapper(BCOrgMapper bcOrgMapper) {
        this.bcOrgMapper = bcOrgMapper;
    }

    @Transactional
    public void createChannel(JSONObject jsonObject)throws BaseRuntimeException {
        String clusterId = jsonObject.getString("clusterId");
        ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
        logger.debug("[channel->create] create channel，get cluster id is:{}",clusterId);

        BCCluster bcCluster = bcClusterMapper.getClusterById(clusterId);
        String clusterName = bcCluster.getClusterName();
        ValidatorUtils.isNotNull(clusterName, ValidatorResultCode.VALIDATOR_CLUSTER_NAME_NULL);
        logger.info("[channel->create] create channel，get cluster name is:{}",clusterName);

        String channelName = jsonObject.getString("channelName");
        ValidatorUtils.isNotNull(channelName, ValidatorResultCode.VALIDATOR_CHANNEL_NAME_NULL);
        //判断通道名称是否匹配
        if(!ValidatorUtils.isMatches(channelName,ValidatorUtils.FABRIC_CHANNEL_NAME_REGEX)){
            logger.error("[channel->create] create channel，组织名称错误，获取到的组织名称为:{}",channelName);
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CHANNEL_NAME_NOT_MATCH);
        }

        //组成通道的组织名称不能为空
        JSONArray orgIds = jsonObject.getJSONArray("orgIds");
        ValidatorUtils.isNotNull(orgIds, ValidatorResultCode.VALIDATOR_ORG_ID_NULL);
        List<String> orgIdList = orgIds.toJavaList(String.class);
        //根据组织的主键列表查询组织集合
        List<BCOrg> bcOrgList = bcOrgMapper.getOrgByOrgIds(orgIdList);
        if(bcOrgList.size() != orgIds.size()){
            logger.error("[channel->create] create channel，get the org list is not match parameters, get the org list size is:{}, but expect value is ",bcOrgList.size(),orgIds.size());
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_CHANNEL_ORG_LIST_ERROR);
        }
        //TODO 未做名称重复检查

        BCChannel bcChannel = new BCChannel();
        bcChannel.setChannelName(channelName);
        bcChannel.setClusterId(clusterId);
        bcChannel.setClusterName(clusterName);
        bcChannel.setId(StringUtils.getId());
        bcChannel.setIsBlockListener(0);
        bcChannel.setChannelStatus(1);
        bcChannelMapper.insertChannel(bcChannel);

        //添加到脚本执行的队列中
        //把对应的组织对象添加至脚本的执行队列中，等待执行
        //主要用于生成Orderer组织的MSP证书和TLS的证书
        //在同一个集群中，创建Orderer组织的脚本得确保是优先执行；理论上队列的特性只要确定该任务是先加入队列即可。
        //如果生产环境在集群、并发情况下，并且添加Peer组织无法控制；可以按照集群的状态决定是否创建peer的组织。
        BlockChainFabricChannelEventSource blockChainFabricChannelEventSource = new BlockChainFabricChannelEventSource();
        blockChainFabricChannelEventSource.setBcChannel(bcChannel);
        blockChainFabricChannelEventSource.setBcCluster(bcCluster);
        blockChainFabricChannelEventSource.setBcOrgs(bcOrgList);
        boolean flag = BlockChainShellQueueUtils.add(blockChainFabricChannelEventSource);
        if(!flag){
            logger.error("[async] 通道加入任务队列错误，请确认错误信息。");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_ORG_QUEUE_ERROR);
        }
    }
}
