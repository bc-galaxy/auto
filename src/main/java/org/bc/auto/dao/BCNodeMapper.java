package org.bc.auto.dao;

import org.apache.ibatis.annotations.Param;
import org.bc.auto.model.entity.BCNode;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface BCNodeMapper {

    int insertNode(BCNode bcNode);

    List<BCNode> getAllNode();

    List<BCNode> getNodeByNodeName(@Param(value = "nodeName") String nodeName);

    List<BCNode> getNodeByNodeNameAndCluster(@Param(value = "nodeName") String orgName,@Param(value = "clusterId") String clusterId);

}
