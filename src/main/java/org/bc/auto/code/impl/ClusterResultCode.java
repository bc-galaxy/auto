package org.bc.auto.code.impl;

import org.bc.auto.code.ResultCode;

public enum ClusterResultCode implements ResultCode {


    CREATE_PEER_FAIL(10040, "创建PEER节点失败")
    ;


    private int code;
    private String msg;
    ClusterResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return 0;
    }

    @Override
    public String getMsg() {
        return null;
    }
}
