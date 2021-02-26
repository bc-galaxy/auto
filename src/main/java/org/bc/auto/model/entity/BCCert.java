package org.bc.auto.model.entity;

import java.io.Serializable;

public class BCCert implements Serializable {
    private static final long serialVersionUID = 1520423954949793297L;
    private String id;

    private String certName;

    private String certPubKey;

    private String certPriKey;

    //组织的根MSP证书
    private String certCaCert;

    private String certTlsPubKey;

    private String certTlsPriKey;

    //1:Orderer组织的管理员证书；2:Orderer组织的用户证书；3:Org组织的管理员证书；4:Org组织的用户证书
    //5:Org组织的节点证书；6:Orderer组织的节点证书。
    //证书类型之间相互为互斥证书
    private Integer certType;

    //证书状态，1：正常，2：已注销，3：注销中
    private Integer certStatus;

    private String clusterId;

    private String orgId;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(Integer certStatus) {
        this.certStatus = certStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getCertPubKey() {
        return certPubKey;
    }

    public void setCertPubKey(String certPubKey) {
        this.certPubKey = certPubKey;
    }

    public String getCertPriKey() {
        return certPriKey;
    }

    public void setCertPriKey(String certPriKey) {
        this.certPriKey = certPriKey;
    }

    public String getCertCaCert() {
        return certCaCert;
    }

    public void setCertCaCert(String certCaCert) {
        this.certCaCert = certCaCert;
    }

    public String getCertTlsPubKey() {
        return certTlsPubKey;
    }

    public void setCertTlsPubKey(String certTlsPubKey) {
        this.certTlsPubKey = certTlsPubKey;
    }

    public String getCertTlsPriKey() {
        return certTlsPriKey;
    }

    public void setCertTlsPriKey(String certTlsPriKey) {
        this.certTlsPriKey = certTlsPriKey;
    }

    public Integer getCertType() {
        return certType;
    }

    public void setCertType(Integer certType) {
        this.certType = certType;
    }
}
