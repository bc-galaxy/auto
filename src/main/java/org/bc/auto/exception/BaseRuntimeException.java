package org.bc.auto.exception;

import org.bc.auto.code.ResultCode;
import org.bc.auto.code.impl.SystemResultCode;
import org.bc.auto.model.vo.Result;

public abstract class BaseRuntimeException extends RuntimeException{

    private static final long serialVersionUID = 5887453810918892702L;
    private Integer code;
    private String msg;

    /**
     * @Description: 运行期异常无参构造方法
     * @author: Mason
     * @param :
     * @return:
     * @createDate: 2019-09-23
     */
    public BaseRuntimeException() {
        super(SystemResultCode.SYSTEM_ERROR.getMsg());
        this.code = SystemResultCode.SYSTEM_ERROR.getCode();
        this.msg = SystemResultCode.SYSTEM_ERROR.getMsg();
    }

    /**
     * @Description: 运行期异常构造方法
     * @author: Mason
     * @param : code 构造传参，异常编码
     * @param : msg 构造传参，异常信息
     * @return:
     * @createDate: 2019-09-23
     */
    public BaseRuntimeException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    /**
     * @Description: 运行期异常构造方法
     * @author: Mason
     * @param : resultCode 构造传参对象
     * @return:
     * @createDate: 2019-09-23
     */
    public BaseRuntimeException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Result getExceptionResult(){
        Result result = new Result();
        result.setCode(this.code);
        result.setMsg(this.msg);
        return result;
    }

}
