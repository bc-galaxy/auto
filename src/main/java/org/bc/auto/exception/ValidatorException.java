package org.bc.auto.exception;

import org.bc.auto.code.ResultCode;
import org.bc.auto.code.impl.SystemResultCode;

public class ValidatorException extends BaseRuntimeException{

    public ValidatorException() {
        super(SystemResultCode.SYSTEM_ERROR);
    }

    public ValidatorException(ResultCode resultCode) {
        super(resultCode);
    }

    public ValidatorException(String msg) {
        super(SystemResultCode.SYSTEM_ERROR.getCode(), msg);
    }

    public ValidatorException(int code, String msg) {
        super(code, msg);
    }

}
