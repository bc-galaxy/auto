package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCNodeMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.model.entity.BlockChainArrayList;
import org.bc.auto.model.entity.BlockChainNetwork;
import org.bc.auto.service.NodeService;
import org.bc.auto.utils.BlockChainShellQueueUtils;
import org.bc.auto.utils.DateUtils;
import org.bc.auto.utils.ValidatorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NodeServiceImpl implements NodeService {

    private static final Logger logger = LoggerFactory.getLogger(NodeServiceImpl.class);

    private BCNodeMapper bcNodeMapper;

    @Autowired
    public void setBcOrgMapper(BCNodeMapper bcNodeMapper) {
        this.bcNodeMapper = bcNodeMapper;
    }

    public void createNode(JSONArray jsonArray)throws BaseRuntimeException {
        //假如列表中有节点元素
        if(ValidatorUtils.isGreaterThanZero(jsonArray.size())){

            //定义节点对象集合
            List<BCNode> bcNodeInsertList = new ArrayList<>();

            BlockChainArrayList bcNodeBlockChainArrayList = new BlockChainArrayList<BCNode>();

            //对元素进行循环处理
            for(int i=0;i<jsonArray.size();i++){
                //取出列表中的节点元素
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String clusterId = jsonObject.getString("clusterId");
                ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
                logger.debug("[node->create] 创建节点，获取的集群编号信息为:{}",clusterId);

                String nodeName = jsonObject.getString("nodeName");
                ValidatorUtils.isNotNull(nodeName, ValidatorResultCode.VALIDATOR_NODE_NAME_NULL);
                //判断节点名称是否匹配
                if(!ValidatorUtils.isMatches(nodeName,ValidatorUtils.FABRIC_PEER_NAME_REGEX)){
                    logger.error("[node->create] 创建节点，节点名称错误，获取到的节点名称为:{}",nodeName);
                    throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_NAME_NOT_MATCH);
                }
                logger.info("[node->create] 创建节点，获取的节点名称信息为:{}",nodeName);
                //检查节点是否存在
                List<BCNode> bcNodeList = bcNodeMapper.getNodeByNodeNameAndCluster(nodeName,clusterId);
                if(!ValidatorUtils.isNull(bcNodeList)){
                    logger.warn("[node->create] 创建区块链集群，节点名'{}'已存在",nodeName);
                    throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_NAME_RE);
                }

                int nodeType = jsonObject.getIntValue("nodeType");
                String orgId = jsonObject.getString("orgId");

                BCNode bcNode = new BCNode();
                bcNode.setNodeName(nodeName);
                bcNode.setClusterId(clusterId);
                bcNode.setOrgId(orgId);
                bcNode.setNodeType(nodeType);
                bcNode.setCreateTime(DateUtils.getCurrentMillisTimeStamp());

                bcNodeInsertList.add(bcNode);
            }
            int nodeResult = bcNodeMapper.insertNode(bcNodeInsertList);
            //如果集群成功入库
            if(!ValidatorUtils.isGreaterThanZero(nodeResult)){
                logger.error("[node->create] 创建节点，插入数据库失败，请确认。");
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_INSERT_ERROR);
            }
            bcNodeBlockChainArrayList.seteList(bcNodeInsertList);
            boolean flag = BlockChainShellQueueUtils.add(bcNodeBlockChainArrayList);
            if(!flag){
                logger.error("[node->create] 组织加入任务队列错误，请确认错误信息。");
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_QUEUE_ERROR);
            }
        }
    }



}
