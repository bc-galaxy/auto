package org.bc.auto.model.entity;

public class Orderer {
    private Integer id;

    private String ordererName;

    private String ordererDomain;

    private String ordererAliasName;

    private String ordererIp;

    private Integer ordererPort;

    private Integer ordererEventPort;

    private Integer ordererStatus;

    private Integer orgOrdererId;

    private Long createTime;

    private Integer createUserId;

    private Long updateTime;

    private String ordererTlsPath;

    private Integer clusterId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOrdererName() {
        return ordererName;
    }

    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName == null ? null : ordererName.trim();
    }

    public String getOrdererDomain() {
        return ordererDomain;
    }

    public void setOrdererDomain(String ordererDomain) {
        this.ordererDomain = ordererDomain == null ? null : ordererDomain.trim();
    }

    public String getOrdererAliasName() {
        return ordererAliasName;
    }

    public void setOrdererAliasName(String ordererAliasName) {
        this.ordererAliasName = ordererAliasName == null ? null : ordererAliasName.trim();
    }

    public String getOrdererIp() {
        return ordererIp;
    }

    public void setOrdererIp(String ordererIp) {
        this.ordererIp = ordererIp == null ? null : ordererIp.trim();
    }

    public Integer getOrdererPort() {
        return ordererPort;
    }

    public void setOrdererPort(Integer ordererPort) {
        this.ordererPort = ordererPort;
    }

    public Integer getOrdererEventPort() {
        return ordererEventPort;
    }

    public void setOrdererEventPort(Integer ordererEventPort) {
        this.ordererEventPort = ordererEventPort;
    }

    public Integer getOrdererStatus() {
        return ordererStatus;
    }

    public void setOrdererStatus(Integer ordererStatus) {
        this.ordererStatus = ordererStatus;
    }

    public Integer getOrgOrdererId() {
        return orgOrdererId;
    }

    public void setOrgOrdererId(Integer orgOrdererId) {
        this.orgOrdererId = orgOrdererId;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(Integer createUserId) {
        this.createUserId = createUserId;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public String getOrdererTlsPath() {
        return ordererTlsPath;
    }

    public void setOrdererTlsPath(String ordererTlsPath) {
        this.ordererTlsPath = ordererTlsPath == null ? null : ordererTlsPath.trim();
    }

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }
}