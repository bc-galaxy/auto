package org.bc.auto.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.NodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/node")
public class NodeController {

    private static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    private NodeService nodeService;
    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @PostMapping("/create")
    public Result createNodes(@RequestBody JSONArray jsonArray) {
        logger.debug("[node->create] 用户请求区块链节点create方法");
        nodeService.createNode(jsonArray);
//        clusterService.createCluster(jsonObject);
        return Result.success();
    }

    @PostMapping("/join")
    public Result joinChannel(@RequestBody JSONObject jsonObject) {
        logger.debug("[node->join] 用户请求区块链节点加入通道");
        nodeService.joinChannel(jsonObject);
        return Result.success();
    }

}
