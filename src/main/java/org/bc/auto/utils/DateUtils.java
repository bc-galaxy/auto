package org.bc.auto.utils;

import org.bc.auto.code.ResultCode;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.exception.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    private static final Logger logger = LoggerFactory.getLogger(DateUtils.class);

    private static final String DATE_DAY_FORMAT  = "yyyy-MM-dd";
    private static final String DATE_SECOND_FORMAT  = "yyyy-MM-dd HH:mm:ss";


    /**
     * 获取当前系统的毫秒时间戳
     * @return
     */
    public static long getCurrentMillisTimeStamp(){
        long timeStamp = System.currentTimeMillis();
        logger.debug("获取当前系统的毫秒级时间戳：{}",timeStamp);
        return timeStamp;
    }

    /**
     * 获取当前系统的秒时间戳
     * @return
     */
    public static long getCurrentSecondTimeStamp(){
        long timeStamp = System.currentTimeMillis();
        timeStamp = timeStamp/1000;
        logger.debug("获取当前系统的秒级时间戳：{}",timeStamp);
        return timeStamp;
    }

    /**
     * 获取指定的时间戳转化为天(必须为13位的毫秒时间戳)
     * @param timeStamp
     * @return
     */
    public static String getTimeStampToDateDayString(Long timeStamp, ResultCode...resultCodes){
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_DAY_FORMAT);
        ResultCode resultCode = ValidatorUtils.isNull(resultCodes)? ValidatorResultCode.VALIDATOR_VALUE_NOT_NULL:resultCodes[0];

        if(ValidatorUtils.isNull(timeStamp)){
            throw new ValidatorException(resultCode);
        }
        logger.debug("当前传入的参数值为：{}",timeStamp);
        String dateFormat = sdf.format(new Date(timeStamp));
        return dateFormat;
    }

    /**
     * 获取指定的时间戳转化为秒(必须为13位的毫秒时间戳)
     * @param timeStamp
     * @return
     */
    public static String getTimeStampToDateSecondString(Long timeStamp, ResultCode...resultCodes){
        ResultCode resultCode = ValidatorUtils.isNull(resultCodes)?ValidatorResultCode.VALIDATOR_VALUE_NOT_NULL:resultCodes[0];

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_SECOND_FORMAT);

        if(ValidatorUtils.isNull(timeStamp)){
            throw new ValidatorException(resultCode);
        }
        logger.debug("当前传入的参数值为：{}",timeStamp);
        String dateFormat = sdf.format(new Date(timeStamp));
        return dateFormat;
    }

    /**
     * 获取当前时间秒的字符串格式
     * @return
     */
    public static String getCurrentDateSecondString(){
        return getTimeStampToDateSecondString(getCurrentMillisTimeStamp());
    }

    /**
     * 获取当前时间天的字符串格式
     * @return
     */
    public static String getCurrentDateDayString(){
        return getTimeStampToDateSecondString(getCurrentMillisTimeStamp());
    }

}
