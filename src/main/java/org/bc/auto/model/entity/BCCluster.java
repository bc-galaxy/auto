package org.bc.auto.model.entity;

import java.io.Serializable;

public class BCCluster implements Serializable,BlockChainNetwork{
    private static final long serialVersionUID = 4317978403273155155L;

    private String id;

    private String clusterName;

    //安装状态从1往后数
    private Integer installStatus;

    private Long createTime;

    private Long expiresTime;

    //集群的类型，1：Fabric，2：QuoRom
    private Integer clusterType;

    //安装的版本（各种版本，如：Fabric:1.4.5）
    private String clusterVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
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

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
    }
}