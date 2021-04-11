package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.model.vo.Result;
import org.bc.auto.service.ChannelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/channel")
public class ChannelController {

    private static final Logger logger = LoggerFactory.getLogger(ChannelController.class);

    private ChannelService channelService;
    @Autowired
    public void setChannelService(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PostMapping("/create")
    public Result createChannel(@RequestBody JSONObject jsonObject) {
        logger.debug("[channel->create] 用户请求区块链create方法");

        channelService.createChannel(jsonObject);
        return Result.success();
    }

}
