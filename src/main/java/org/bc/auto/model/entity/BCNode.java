package org.bc.auto.model.entity;

import org.bc.auto.listener.source.BlockChainEventSource;

import java.io.Serializable;

public class BCNode implements Serializable {
    private static final long serialVersionUID = -1257130671388672087L;

    private String id;

    private String clusterId;

    private String orgId;

    private String orgName;

    private String nodeName;

    //节点类型 1：Orderer组织的节点，2：Org组织的节点
    private Integer nodeType;

    private String nodeIp;

    private Integer nodePort;

    private Integer nodeEventPort;

    private String nodeTlsPath;

    private long createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public Integer getNodeType() {
        return nodeType;
    }

    public void setNodeType(Integer nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public Integer getNodePort() {
        return nodePort;
    }

    public void setNodePort(Integer nodePort) {
        this.nodePort = nodePort;
    }

    public Integer getNodeEventPort() {
        return nodeEventPort;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public void setNodeEventPort(Integer nodeEventPort) {
        this.nodeEventPort = nodeEventPort;
    }

    public String getNodeTlsPath() {
        return nodeTlsPath;
    }

    public void setNodeTlsPath(String nodeTlsPath) {
        this.nodeTlsPath = nodeTlsPath;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
