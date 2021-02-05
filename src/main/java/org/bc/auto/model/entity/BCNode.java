package org.bc.auto.model.entity;

import java.io.Serializable;

public class BCNode implements Serializable {
    private static final long serialVersionUID = -1257130671388672087L;

    private String id;

    private String clusterId;

    private String orgId;

    //节点类型 1：Orderer组织的节点，2：Org组织的节点
    private Integer nodeType;

    private String nodeIp;

    private Integer nodePort;

    private Integer nodeEventPort;

    private String nodeTlsPath;

}
