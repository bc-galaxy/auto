package org.bc.auto.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.dao.BCClusterMapper;
import org.bc.auto.dao.BCNodeMapper;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.bc.auto.model.entity.BCCluster;
import org.bc.auto.model.entity.BCNode;
import org.bc.auto.model.entity.BlockChainNodeList;
import org.bc.auto.service.NodeService;
import org.bc.auto.utils.*;
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

    private BCClusterMapper bcClusterMapper;
    @Autowired
    public void setBcClusterMapper(BCClusterMapper bcClusterMapper) {
        this.bcClusterMapper = bcClusterMapper;
    }

    public BlockChainNodeList createNode(JSONArray jsonArray)throws BaseRuntimeException {

        //确认传入的节点列表是非空集合
        ValidatorUtils.isNotNull(jsonArray,ValidatorResultCode.VALIDATOR_NODE_ARRAY_ERROR);

        //定义节点对象集合
        List<BCNode> bcNodeInsertList = new ArrayList<>();
        //定义事件监听的返回对象
        BlockChainNodeList bcNodeBlockChainArrayList = new BlockChainNodeList<BCNode>();

            //对元素进行循环处理
        for(int i=0;i<jsonArray.size();i++){
            //取出列表中的节点元素
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            //获取集群编号，该节点属于哪个网络下。
            String clusterId = jsonObject.getString("clusterId");
            ValidatorUtils.isNotNull(clusterId, ValidatorResultCode.VALIDATOR_CLUSTER_ID_NULL);
            logger.debug("[node->create] create blockchain's node，get the cluster id is:{}",clusterId);
            //获取创建的节点名称。
            String nodeName = jsonObject.getString("nodeName");
            ValidatorUtils.isNotNull(nodeName, ValidatorResultCode.VALIDATOR_NODE_NAME_NULL);
            //判断节点名称是否匹配
            if(!ValidatorUtils.isMatches(nodeName,ValidatorUtils.FABRIC_PEER_NAME_REGEX)){
                logger.error("[node->create] create blockchain's node，node name error，make sure node name '{}' accord with naming convention",nodeName);
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_NAME_NOT_MATCH);
            }
            logger.debug("[node->create] create blockchain's node，get the node name is:{}",nodeName);
            //检查节点是否存在
            List<BCNode> bcNodeList = bcNodeMapper.getNodeByNodeNameAndCluster(nodeName,clusterId);
            if(!ValidatorUtils.isNull(bcNodeList)){
                logger.error("[org->create] create blockchain's node，make sure node name '{}' is not exists",nodeName);
                throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_NAME_RE);
            }

            int nodeType = jsonObject.getIntValue("nodeType");
            String orgId = jsonObject.getString("orgId");
            logger.info("[node->create] create blockchain's node, get the node name is :{},cluster id is :{}, org id is :{}",
                    nodeName, clusterId, orgId);
            BCNode bcNode = new BCNode();
            bcNode.setId(StringUtils.getId());
            bcNode.setNodeName(nodeName);
            bcNode.setClusterId(clusterId);
            bcNode.setOrgId(orgId);
            bcNode.setNodeType(nodeType);
            bcNode.setOrgName(jsonObject.getString("orgName"));
            bcNode.setCreateTime(DateUtils.getCurrentMillisTimeStamp());

            bcNodeInsertList.add(bcNode);
        }
        int nodeResult = bcNodeMapper.insertNodeList(bcNodeInsertList);
        //如果集群成功入库
        if(!ValidatorUtils.isGreaterThanZero(nodeResult)){
            logger.error("[node->create] create blockchain's node，insert database error.");
            throw new ValidatorException(ValidatorResultCode.VALIDATOR_NODE_INSERT_ERROR);
        }

        bcNodeBlockChainArrayList.seteList(bcNodeInsertList);
        return bcNodeBlockChainArrayList;
    }

    public int updateNode(BCNode bcNode)throws BaseRuntimeException{
        return bcNodeMapper.updateNode(bcNode);
    }

}
