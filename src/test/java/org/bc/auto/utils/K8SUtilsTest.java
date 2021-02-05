package org.bc.auto.utils;

import org.bc.auto.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class K8SUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(K8SUtilsTest.class);

    @Test
    public void testGetPdbNameError(){
        try{
            logger.info(K8SUtils.getPdbName(""));
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testGetPdbName(){
        try{
            logger.info(K8SUtils.getPdbName("aaa"));
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }
}
