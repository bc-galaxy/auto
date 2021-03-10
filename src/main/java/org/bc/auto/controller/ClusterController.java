package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.entity.BCCluster;
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

    @PostMapping("/create")
    public Result createChain(@RequestBody JSONObject jsonObject) {
        logger.debug("[cluster->create] request is to create blockchain's cluster, create k8s namespace、pv、pvc too");
        JSONObject jsonObjectResult = new JSONObject();
        BCCluster bcCluster = clusterService.createCluster(jsonObject);
        logger.info("[cluster->create] create blockchain's cluster end, cluster info => id is:{}, name is:{}, version is:{}",bcCluster.getId(),bcCluster.getClusterName(),bcCluster.getClusterVersion());
        jsonObjectResult.put("clusterId",bcCluster.getId());
        jsonObjectResult.put("clusterName",bcCluster.getClusterName());
        return Result.success(jsonObjectResult);
    }

    @GetMapping("/all")
    public Result getClusters() {
        return Result.success(clusterService.getBCClusterList());
    }
}
