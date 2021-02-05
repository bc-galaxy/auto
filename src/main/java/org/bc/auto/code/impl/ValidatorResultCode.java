package org.bc.auto.code.impl;

import org.bc.auto.code.ResultCode;

public enum ValidatorResultCode  implements ResultCode {

    VALIDATOR_VALUE_NOT_NULL(10001,"获取的数据值为空,请检查相关数据项"),
    TEST_VALIDATOR_STRING_VALUE_NOT_NULL(110001,"[Test]字符串为空的自定义返回"),
    TEST_VALIDATOR_OBJECT_VALUE_NOT_NULL(110002,"[Test]对象为空的自定义返回"),
    VALIDATOR_RESULT_ERROR(10002, "操作结果与期望的不匹配，请检查参数是否正确"),

    VALIDATOR_FORMAT_ERROR(10003, "获取的数据格式错误"),
    VALIDATOR_VALUE_ERROR(10004,"数据值错误，请检查输入的参数项"),

    VALIDATOR_CLUSTER_NAME_NULL(10005,"获取集群名称为空"),
    VALIDATOR_CLUSTER_VERSION_NULL(10006,"获取集群版本为空"),
    VALIDATOR_CLUSTER_TYPE_NULL(10007,"获取集群类型为空"),
    VALIDATOR_CLUSTER_INSERT_ERROR(10008,"集群插入错误"),
    VALIDATOR_CLUSTER_ID_NULL(10009,"获取集群编号为空"),
    VALIDATOR_CLUSTER_NAME_RE(10010,"集群名称重复，请确定"),
    VALIDATOR_CLUSTER_NAME_NOT_MATCH(10012,"集群名称不符合规则，请确定"),

    VALIDATOR_ORG_NAME_NULL(10013,"获取组织名称为空"),
    VALIDATOR_ORG_NAME_NOT_MATCH(10014,"组织名称不符合规则，请确定"),
    VALIDATOR_ORG_NAME_RE(10015,"组织已存在"),
    VALIDATOR_ORG_QUEUE_ERROR(10016,"组织任务加入队列失败"),
    VALIDATOR_ORG_INSERT_ERROR(10017,"组织插入数据库错误"),
    ;

    private int code;
    private String msg;
    ValidatorResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }

}
