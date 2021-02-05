package org.bc.auto.model.entity;

public class CertUser {
    private Integer id;

    private String certUserName;

    private String certUserAliasName;

    private String certUserPubKey;

    private String certUserPriKey;

    /**
     * 组织msp根证书
     */
    private String certUserCaCert;

    private String certTlsPubKey;

    private String certTlsPriKey;

    private Long createTime;

    private Integer certUserType;

    private Integer orgId;

    private String orgName;

    private String orgAliasName;

    private Integer orgType;

    private Integer clusterId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCertUserName() {
        return certUserName;
    }

    public void setCertUserName(String certUserName) {
        this.certUserName = certUserName == null ? null : certUserName.trim();
    }

    public String getCertUserAliasName() {
        return certUserAliasName;
    }

    public void setCertUserAliasName(String certUserAliasName) {
        this.certUserAliasName = certUserAliasName == null ? null : certUserAliasName.trim();
    }

    public String getCertUserPubKey() {
        return certUserPubKey;
    }

    public void setCertUserPubKey(String certUserPubKey) {
        this.certUserPubKey = certUserPubKey == null ? null : certUserPubKey.trim();
    }

    public String getCertUserPriKey() {
        return certUserPriKey;
    }

    public void setCertUserPriKey(String certUserPriKey) {
        this.certUserPriKey = certUserPriKey == null ? null : certUserPriKey.trim();
    }

    public String getCertUserCaCert() {
        return certUserCaCert;
    }

    public void setCertUserCaCert(String certUserCaCert) {
        this.certUserCaCert = certUserCaCert;
    }

    public String getCertTlsPubKey() {
        return certTlsPubKey;
    }

    public void setCertTlsPubKey(String certTlsPubKey) {
        this.certTlsPubKey = certTlsPubKey == null ? null : certTlsPubKey.trim();
    }

    public String getCertTlsPriKey() {
        return certTlsPriKey;
    }

    public void setCertTlsPriKey(String certTlsPriKey) {
        this.certTlsPriKey = certTlsPriKey == null ? null : certTlsPriKey.trim();
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getCertUserType() {
        return certUserType;
    }

    public void setCertUserType(Integer certUserType) {
        this.certUserType = certUserType;
    }

    public Integer getOrgId() {
        return orgId;
    }

    public void setOrgId(Integer orgId) {
        this.orgId = orgId;
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

    public Integer getOrgType() {
        return orgType;
    }

    public void setOrgType(Integer orgType) {
        this.orgType = orgType;
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
}