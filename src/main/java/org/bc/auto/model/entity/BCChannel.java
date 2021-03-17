package org.bc.auto.model.entity;

import org.bc.auto.listener.source.BlockChainEventSource;

import java.io.Serializable;

public class BCChannel implements Serializable {
    private static final long serialVersionUID = 8557422491575196537L;

    private String id;

    private String channelName;

    private int channelStatus;

    private int isBlockListener;

    private String clusterId;

    private String clusterName;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public int getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(int channelStatus) {
        this.channelStatus = channelStatus;
    }

    public int getIsBlockListener() {
        return isBlockListener;
    }

    public void setIsBlockListener(int isBlockListener) {
        this.isBlockListener = isBlockListener;
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
