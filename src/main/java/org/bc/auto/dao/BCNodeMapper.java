package org.bc.auto.dao;

import org.apache.ibatis.annotations.Param;
import org.bc.auto.model.entity.BCNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCNodeMapper {

    int insertNodeList(List<BCNode> bcNodeList);

    int updateNode(BCNode bcNode);

    List<BCNode> getAllNode();

    List<BCNode> getNodeByPeerIds(List<String> peerIds);

    List<BCNode> getNodeByNodeName(@Param(value = "nodeName") String nodeName);

    List<BCNode> getNodeByNodeNameAndCluster(@Param(value = "nodeName") String nodeName,@Param(value = "clusterId") String clusterId);

    List<BCNode> getNodeByNodeTypeAndCluster(@Param(value = "nodeType") int nodeType,@Param(value = "clusterId") String clusterId);

}
