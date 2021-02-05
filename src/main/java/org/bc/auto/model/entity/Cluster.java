package org.bc.auto.model.entity;

public class Cluster {
    private Integer id;

    private String clusterName;

    private String clusterSuffix;

    private String clusterDesc;

    private Integer installStatus;

    private Long createTime;

    private Long updateTime;

    private Long expiresTime;

    private Integer clusterType;

    private Integer consensusStrategy;

    private Integer leagueId;

    private String clusterVersion;

    private String serviceName;

    private String serviceConfig;

    private int companyId;

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName == null ? null : clusterName.trim();
    }

    public String getClusterSuffix() {
        return clusterSuffix;
    }

    public void setClusterSuffix(String clusterSuffix) {
        this.clusterSuffix = clusterSuffix == null ? null : clusterSuffix.trim();
    }

    public String getClusterDesc() {
        return clusterDesc;
    }

    public void setClusterDesc(String clusterDesc) {
        this.clusterDesc = clusterDesc == null ? null : clusterDesc.trim();
    }

    public Integer getInstallStatus() {
        return installStatus;
    }

    public void setInstallStatus(Integer installStatus) {
        this.installStatus = installStatus;
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

    public Long getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(Long expiresTime) {
        this.expiresTime = expiresTime;
    }

    public Integer getClusterType() {
        return clusterType;
    }

    public void setClusterType(Integer clusterType) {
        this.clusterType = clusterType;
    }

    public Integer getConsensusStrategy() {
        return consensusStrategy;
    }

    public void setConsensusStrategy(Integer consensusStrategy) {
        this.consensusStrategy = consensusStrategy;
    }

    public Integer getLeagueId() {
        return leagueId;
    }

    public void setLeagueId(Integer leagueId) {
        this.leagueId = leagueId;
    }

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion == null ? null : clusterVersion.trim();
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName == null ? null : serviceName.trim();
    }

    public String getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(String serviceConfig) {
        this.serviceConfig = serviceConfig == null ? null : serviceConfig.trim();
    }
}