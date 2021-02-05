package org.bc.auto.exception;

import org.bc.auto.code.ResultCode;
import org.bc.auto.code.impl.SystemResultCode;

public class K8SException extends BaseRuntimeException{
    public K8SException() {
        super(SystemResultCode.SYSTEM_ERROR);
    }

    public K8SException(ResultCode resultCode) {
        super(resultCode);
    }

    public K8SException(String msg) {
        super(SystemResultCode.SYSTEM_ERROR.getCode(), msg);
    }

    public K8SException(int code, String msg) {
        super(code, msg);
    }
}
