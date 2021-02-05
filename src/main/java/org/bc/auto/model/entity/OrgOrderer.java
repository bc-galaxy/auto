package org.bc.auto.model.entity;

public class OrgOrderer {
    private Integer id;

    private String orgName;

    private String orgAliasName;

    private String orgMspId;

    private Integer orgIsTls;

    private Integer orgStatus;

    private String orgAddress;

    private Integer createUserId;

    private Long createTime;

    private Long updateTime;

    private Integer clusterId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName == null ? null : orgName.trim();
    }

    public String getOrgAliasName() {
        return orgAliasName;
    }

    public void setOrgAliasName(String orgAliasName) {
        this.orgAliasName = orgAliasName == null ? null : orgAliasName.trim();
    }

    public String getOrgMspId() {
        return orgMspId;
    }

    public void setOrgMspId(String orgMspId) {
        this.orgMspId = orgMspId == null ? null : orgMspId.trim();
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

    public String getOrgAddress() {
        return orgAddress;
    }

    public void setOrgAddress(String orgAddress) {
        this.orgAddress = orgAddress == null ? null : orgAddress.trim();
    }

    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
}