package org.bc.auto.controller;

import com.alibaba.fastjson.JSONObject;
import org.bc.auto.config.BlockChainAutoConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 区块链业务操作类
 * @Package: com.bc.baas.controller
 * @ClassName: BlockChainBusiness
 * @author: mason
 * @email: gaobo@mininglamp.com
 * @createDate: 2021/1/25
 * @Company: 北京明略科技有限公司
 *
 * ---------------------------------------------------------
 * Version    Author    Status    Date
 * V1.0       mason       C    	2021/1/25
 */
@RestController
@RequestMapping("/bc")
public class BlockChainBusinessController {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainBusinessController.class);

    @PostMapping("/invoke")
    public void createChannel(@RequestBody JSONObject param) {

        logger.info("[bc->invoke] 用户请求bc模块中的invoke方法，参数如下 :{}",param.toJSONString());

        logger.info("数值为：{}", BlockChainAutoConstant.NFS_HOST);

    }


}
