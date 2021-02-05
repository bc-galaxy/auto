package org.bc.auto.config;

import org.bc.auto.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainK8SConstantTest {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainK8SConstantTest.class);

    @Test
    public void testGetK8sConfigPath(){
        logger.info("k8s config file path is {}",BlockChainK8SConstant.getK8sConfigPath());
    }
}
