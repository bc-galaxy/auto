package org.bc.auto.model.entity;

public class BCClusterInfo {

    private String clusterVersion;

    private int clusterConsensusType;

    private int ordererCount;

    private String clusterId;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public int getClusterConsensusType() {
        return clusterConsensusType;
    }

    public void setClusterConsensusType(int clusterConsensusType) {
        this.clusterConsensusType = clusterConsensusType;
    }

    public int getOrdererCount() {
        return ordererCount;
    }

    public void setOrdererCount(int ordererCount) {
        this.ordererCount = ordererCount;
    }
}
