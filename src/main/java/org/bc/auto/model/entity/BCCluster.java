package org.bc.auto.model.entity;

import org.bc.auto.listener.source.BlockChainEventSource;

import java.io.Serializable;

public class BCCluster implements Serializable {
    private static final long serialVersionUID = 4317978403273155155L;

    private String id;

    private String clusterName;

    //安装状态从1往后数,1:初始状态， 2：创建中， 3：创建成功， 4创建失败
    private Integer installStatus;

    private int ordererCount;

    private int clusterConsensusType;

    private Long createTime;

    private Long expiresTime;

    //集群的类型，1：Fabric，2：QuoRom
    private Integer clusterType;

    //安装的版本（各种版本，如：Fabric:1.4.5）
    private String clusterVersion;

    private int stateDbType;

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

    public int getOrdererCount() {
        return ordererCount;
    }

    public void setOrdererCount(int ordererCount) {
        this.ordererCount = ordererCount;
    }

    public int getClusterConsensusType() {
        return clusterConsensusType;
    }

    public void setClusterConsensusType(int clusterConsensusType) {
        this.clusterConsensusType = clusterConsensusType;
    }

    public int getStateDbType() {
        return stateDbType;
    }

    public void setStateDbType(int stateDbType) {
        this.stateDbType = stateDbType;
    }
}
