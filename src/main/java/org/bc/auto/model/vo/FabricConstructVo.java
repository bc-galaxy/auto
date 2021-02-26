package org.bc.auto.model.vo;

public class FabricConstructVo {

    private String nodeName;
    private String orgName;
    private int nodePort;
    private String nodeK8sRole;
    private String nameSapce;
    private int monitorPort;
    private int chainCodePort;
    private String imageName;
    private String orgMspId;
    private String tlsEnable;
    private String certPath;
    private String dataPath;
    private String nodeDomain;
    private int stateDbType;
    private String clusterVersion;

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public int getStateDbType() {
        return stateDbType;
    }

    public void setStateDbType(int stateDbType) {
        this.stateDbType = stateDbType;
    }

    public String getNodeDomain() {
        return nodeDomain;
    }

    public void setNodeDomain(String nodeDomain) {
        this.nodeDomain = nodeDomain;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public String getNodeK8sRole() {
        return nodeK8sRole;
    }

    public void setNodeK8sRole(String nodeK8sRole) {
        this.nodeK8sRole = nodeK8sRole;
    }

    public String getNameSapce() {
        return nameSapce;
    }

    public void setNameSapce(String nameSapce) {
        this.nameSapce = nameSapce;
    }

    public int getMonitorPort() {
        return monitorPort;
    }

    public void setMonitorPort(int monitorPort) {
        this.monitorPort = monitorPort;
    }

    public int getChainCodePort() {
        return chainCodePort;
    }

    public void setChainCodePort(int chainCodePort) {
        this.chainCodePort = chainCodePort;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getOrgMspId() {
        return orgMspId;
    }

    public void setOrgMspId(String orgMspId) {
        this.orgMspId = orgMspId;
    }

    public String getTlsEnable() {
        return tlsEnable;
    }

    public void setTlsEnable(String tlsEnable) {
        this.tlsEnable = tlsEnable;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
