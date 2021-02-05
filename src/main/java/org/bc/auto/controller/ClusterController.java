package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    private static final Logger logger = LoggerFactory.getLogger(ClusterController.class);

    private ClusterService clusterService;
    @Autowired
    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @GetMapping("/create")
    public Result createCluster() {

        logger.info("[cluster->create] 用户请求cluster模块中的create方法");
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("clusterName","bc-auto-cluster");
        jsonObject.put("clusterVersion","1.4.5");
        jsonObject.put("clusterType","1");

        clusterService.createCluster(jsonObject);
        return Result.success();
    }

    @GetMapping("/all")
    public Result getClusters() {
        return Result.success(clusterService.getBCClusterList());
    }
}
