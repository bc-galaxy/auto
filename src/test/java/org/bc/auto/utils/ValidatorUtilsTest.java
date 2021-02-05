package org.bc.auto.utils;

import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ValidatorUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(ValidatorUtilsTest.class);

    @Test
    public void testStringNotNull(){
        try{
            ValidatorUtils.isNotNull("aaa");
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testIsNullListIsNull(){
        List<String> stringList = new ArrayList<>();
        assert(ValidatorUtils.isNull(stringList));
    }
    @Test
    public void testIsNullListIsNotNull(){
        List<String> stringList = new ArrayList<>();
        stringList.add("string");
        assert(!ValidatorUtils.isNull(stringList));
    }

    @Test
    public void testStringIsNull(){
        try{
            ValidatorUtils.isNotNull("");
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testStringIsNullDefineReturn(){
        try{
            ValidatorUtils.isNotNull("", ValidatorResultCode.TEST_VALIDATOR_STRING_VALUE_NOT_NULL);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testObjectIsNullDefineReturn(){
        try{
            ValidatorUtils.isNotNull(null, ValidatorResultCode.TEST_VALIDATOR_OBJECT_VALUE_NOT_NULL);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testObjectIsNull(){
        try{
            ValidatorUtils.isNotNull(null);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testObjectListIsNull(){
        try{
            List<String> stringList = new ArrayList<>();
            ValidatorUtils.isNotNull(stringList);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testObjectListIsNotNull(){
        try{
            List<String> stringList = new ArrayList<>();
            stringList.add("string");
            ValidatorUtils.isNotNull(stringList);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testObjectNotNull(){
        try{
            ValidatorUtils.isNotNull(new Integer(10));
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    void testIsFalse(){
        try{
            ValidatorUtils.isFalse(false);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    void testNotFalse(){
        try{
            ValidatorUtils.isFalse(true);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    void testIsTrue(){
        try{
            ValidatorUtils.isTrue(true);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    void testNotTrue(){
        try{
            ValidatorUtils.isTrue(false);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    void testIsGreaterThanZero1(){
        assert(!ValidatorUtils.isGreaterThanZero(-1));
    }

    @Test
    void testIsGreaterThanZero2(){
        Integer value = null;
        try{
            assert(!ValidatorUtils.isGreaterThanZero(value));
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    void testIsGreaterThanZero3(){
        System.out.println(String.join("","test","string"));
        assert(ValidatorUtils.isGreaterThanZero(2));
    }

}
