package org.bc.auto.code.impl;

import org.bc.auto.code.ResultCode;

public enum K8SResultCode implements ResultCode {

    CREATE_NAMESPACE_FAIL(20001, "错误的创建了nameSpace"),
    CREATE_PV_FAIL(20002, "错误的创建了pv"),
    CREATE_PVC_FAIL(20003, "错误的创建了pvc"),
    CREATE_POD_FAIL(20004, "错误的创建了PodDisruptionBudget"),
    CREATE_STATEFUL_SET_ERROR(20005, "错误的创建了STATEFUL_SET"),
    CREATE_DEPLOYMENT_ERROR(20006, "错误的创建了DEPLOYMENT"),
    CREATE_SERVICE_ERROR(20007, "错误的创建了SERVICE"),
    DELETE_PERSISTENT_VOLUME_ERROR(20008, "删除PV错误"),
    DELETE_NAMESPACE_ERROR(20009, "删除nameSpace错误"),
    DELETE_PERSISTENT_VOLUME_CLAIM_ERROR(20010, "删除pvc错误"),
    DELETE_POD_DISRUPTION_BUDGET_ERROR(20011, "删除PodDisruptionBudget错误"),
    DELETE_STATEFUL_SET_ERROR(20012, "删除STATEFUL_SET错误"),
    DELETE_DEPLOYMENT_ERROR(20013, "删除DEPLOYMENT错误"),
    DELETE_SERVICE_ERROR(20014, "删除SERVICE错误"),
    READ_SERVICE_ERROR(20015, "读取SERVICE错误"),
    READ_NODE_ERROR(20016, "读取NODE错误"),
    CHECK_K8S_STATUS_ERROR(20017, "检查K8S内的服务节点出错，请确认错误信息"),
    CHECK_POD_ERROR(20018, "检查K8S内POD出错，请确认错误信息"),
    CLEAN_ALL_POD_ERROR(20019, "删除K8S内的服务出错，请确认错误信息"),

    SHELL_EXEC_ERROR(20020, "脚本执行错误，请检查脚本错误信息"),
    SHELL_EXEC_TYPE_ERROR(20021, "没有对应的脚本执行类型，请检查脚本信息是否正确"),
    SHELL_EXEC_PEER_ORG_CERT_ERROR(20022, "创建peer类型的组织错误"),

    CREATE_SVC_FAIL(20023, "错误的创建了svc"),


            ;


    private int code;
    private String msg;
    K8SResultCode(int code, String msg) {
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
