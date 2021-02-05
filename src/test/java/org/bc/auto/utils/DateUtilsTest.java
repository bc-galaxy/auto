package org.bc.auto.utils;

import org.bc.auto.exception.BaseRuntimeException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(DateUtilsTest.class);

    @Test
    public void testGetCurrentMillisTimeStamp(){
        DateUtils.getCurrentMillisTimeStamp();
    }

    @Test
    public void testGetCurrentSecondTimeStamp(){
        DateUtils.getCurrentSecondTimeStamp();
    }

    @Test
    public void testGetMillisTimeStampToDateDayString(){
        logger.info(DateUtils.getTimeStampToDateDayString(DateUtils.getCurrentMillisTimeStamp()));
    }

    @Test
    public void testGetSecondTimeStampToDateDayString(){
        logger.info(DateUtils.getTimeStampToDateDayString(DateUtils.getCurrentSecondTimeStamp()*1000));
    }

    @Test
    public void testGetNullTimeStampToDateDayString(){
        try{
        DateUtils.getTimeStampToDateDayString(null);
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testGetZeroTimeStampToDateDayString(){
        try{
            logger.info(DateUtils.getTimeStampToDateDayString(0L));
        }catch (BaseRuntimeException e){
            logger.error(e.getExceptionResult().toString());
        }
    }

    @Test
    public void testGetSecondTimeStampToDateSecondString(){
        logger.info(DateUtils.getTimeStampToDateSecondString(DateUtils.getCurrentSecondTimeStamp()*1000));
    }



}
