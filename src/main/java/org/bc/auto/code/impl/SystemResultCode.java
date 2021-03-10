package org.bc.auto.code.impl;

import org.bc.auto.code.ResultCode;

public enum SystemResultCode implements ResultCode {


    SYSTEM_SUCCESS(200, "成功"),
    SYSTEM_ERROR(500,"系统异常"),
    SYSTEM_NOT_FOUND(404,"资源不存在"),
    SYSTEM_QUEUE_NULL(701,"资源不存在")
    ;

    private int code;
    private String msg;
    SystemResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return SYSTEM_SUCCESS.code;
    }

    @Override
    public String getMsg() {
        return SYSTEM_SUCCESS.msg;
    }

}
