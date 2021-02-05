package org.bc.auto.utils;

import org.bc.auto.code.ResultCode;
import org.bc.auto.code.impl.ValidatorResultCode;
import org.bc.auto.exception.BaseRuntimeException;
import org.bc.auto.exception.ValidatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

public class ValidatorUtils {
    private static final Logger logger = LoggerFactory.getLogger(ValidatorUtils.class);

    /**
     * 判断值是否为空
     * @param value
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean isNull(Object value) {
        boolean flagValue = ObjectUtils.isEmpty(value);
        return flagValue;
    }

    /**
     * 判断对象不为空
     * @param value
     * @param resultCodes
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean isNotNull(Object value, ResultCode...resultCodes)throws BaseRuntimeException{
        // 判断开发者是否自定义返回结果
        ResultCode resultCode = isNull(resultCodes)? ValidatorResultCode.VALIDATOR_VALUE_NOT_NULL:resultCodes[0];

        //假如传入的值为空
        if(isNull(value)){
            logger.error("传入的参数为空，错误信息为：{}",resultCode.getMsg());
            throw new ValidatorException(resultCode);
        }

        return true;
    }

    /**
     * 判断结果是否为真
     * @param value
     * @param resultCodes
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean isTrue(boolean value,ResultCode ...resultCodes)throws BaseRuntimeException{
        logger.debug("判断传入的参数是否为真，传入的参数值为：{}",value);
        // 判断开发者是否自定义返回结果
        ResultCode resultCode = isNull(resultCodes)?ValidatorResultCode.VALIDATOR_RESULT_ERROR:resultCodes[0];

        if(!value){
            throw new ValidatorException(resultCode);
        }

        return value;

    }

    /**
     * 判断结果是否为假
     * @param value
     * @param resultCodes
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean isFalse(boolean value,ResultCode ...resultCodes)throws BaseRuntimeException{
        logger.debug("判断传入的参数是否为假，传入的参数值为：{}",value);
        // 判断开发者是否自定义返回结果
        ResultCode resultCode = isNull(resultCodes)?ValidatorResultCode.VALIDATOR_RESULT_ERROR:resultCodes[0];

        if(value){
            throw new ValidatorException(resultCode);
        }

        return !value;


    }

    /**
     * 判断传入的值是否大于0
     * @param value
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean isGreaterThanZero(int value)throws BaseRuntimeException{
        logger.debug("判断传入参数是否大于0，参数值为:{}，",value);
        if(value > 0){
            return true;
        }
        return false;
    }

    /**
     * 判断传入的值是否大于0
     * @param value
     * @return
     * @throws BaseRuntimeException
     */
    public static boolean isGreaterThanZero(Integer value){
        if(isNotNull(value) && value.intValue() > 0){
            return true;
        }
        logger.debug("判断传入参数对象的值是否大于0，参数值为:{}，",value.intValue());
        return false;
    }



}
