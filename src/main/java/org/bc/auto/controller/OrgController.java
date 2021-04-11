package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.OrgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/org")
public class OrgController {
    private static final Logger logger = LoggerFactory.getLogger(OrgController.class);

    private OrgService orgService;
    @Autowired
    public void setOrgService(OrgService orgService) {
        this.orgService = orgService;
    }


    @PostMapping("/create")
    public Result createOrg(@RequestBody JSONObject jsonObject) {
        logger.debug("[org->create] 用户请求区块链create方法");
        orgService.createOrg(jsonObject);
//        clusterService.createCluster(jsonObject);
        return Result.success();
    }
}
