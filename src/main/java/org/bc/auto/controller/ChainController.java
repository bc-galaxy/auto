package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chain")
public class ChainController {

    private static final Logger logger = LoggerFactory.getLogger(ChainController.class);

    private ClusterService clusterService;
    @Autowired
    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @PostMapping("/create")
    public Result createChain(@RequestBody JSONObject jsonObject) {
        logger.debug("[chain->create] 用户请求区块链create方法");

        clusterService.createCluster(jsonObject);
        return Result.success();
    }

    @GetMapping("/all")
    public Result getClusters() {
        return Result.success(clusterService.getBCClusterList());
    }
}
