package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org")
public class OrgController {
    private static final Logger logger = LoggerFactory.getLogger(OrgController.class);



    @PostMapping("/create")
    public Result createChain(@RequestBody JSONObject jsonObject) {
        logger.debug("[org->create] 用户请求区块链create方法");

//        clusterService.createCluster(jsonObject);
        return Result.success();
    }
}
