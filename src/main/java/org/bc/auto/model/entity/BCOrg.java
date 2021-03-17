package org.bc.auto.model.entity;

import org.bc.auto.listener.source.BlockChainEventSource;

import java.io.Serializable;

public class BCOrg implements Serializable {

    private static final long serialVersionUID = 5376058759633491230L;

    private String id;

    private String orgName;

    private String orgMspId;

    //是否开启TLS 1:开启，2：未开启
    private Integer orgIsTls;

    //组织的运行状态，1：使用中，2：已注销，3：添加中，4：记录成功（添加数据库），5：添加失败
    private Integer orgStatus;

    //组织类型，1：Orderer组织，2：Org组织
    private Integer orgType;

    private Long createTime;

    private String certId;

    private String clusterId;

    private String clusterName;

    public String getId() {
        return id;
    }

    public String getCertId() {
        return certId;
    }

    public void setCertId(String certId) {
        this.certId = certId;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgMspId() {
        return orgMspId;
    }

    public void setOrgMspId(String orgMspId) {
        this.orgMspId = orgMspId;
    }

    public Integer getOrgIsTls() {
        return orgIsTls;
    }

    public void setOrgIsTls(Integer orgIsTls) {
        this.orgIsTls = orgIsTls;
    }

    public Integer getOrgStatus() {
        return orgStatus;
    }

    public void setOrgStatus(Integer orgStatus) {
        this.orgStatus = orgStatus;
    }

    public Integer getOrgType() {
        return orgType;
    }

    public void setOrgType(Integer orgType) {
        this.orgType = orgType;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}
