
DROP TABLE IF EXISTS bc_cluster;
CREATE TABLE bc_cluster
(
    id VARCHAR(64) NOT NULL COMMENT '主键ID',
    cluster_name VARCHAR(32) NULL DEFAULT NULL COMMENT '集群名称',
    install_status INT(4) NULL DEFAULT NULL COMMENT '集群安装状态',
    create_time BIGINT(15) NULL DEFAULT NULL COMMENT '集群创建时间',
    expires_time BIGINT(15) NULL DEFAULT NULL COMMENT '集群过期时间',
    cluster_type INT(4) NULL DEFAULT NULL COMMENT '集群类型',
    cluster_version VARCHAR(11) NULL DEFAULT NULL COMMENT '集群版本',
    orderer_count INT(11) NULL DEFAULT NULL COMMENT '集群中orderer的总数',
    cluster_consensus_type INT(4) NULL DEFAULT NULL COMMENT '创建共识类型',
    state_db_type INT(4) NULL DEFAULT NULL COMMENT '创建数据库类型',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bc_cert;
CREATE TABLE bc_cert
(
    id VARCHAR(64) NOT NULL COMMENT '主键ID',
    cert_name VARCHAR(32) NULL DEFAULT NULL COMMENT '证书名称',
    cert_pub_key VARCHAR(256)  NULL DEFAULT NULL COMMENT '证书公钥路径',
    cert_pri_key VARCHAR(256)  NULL DEFAULT NULL COMMENT '证书私钥路径',
    cert_ca_cert VARCHAR(256)  NULL DEFAULT NULL COMMENT 'ca证书',
    cert_tls_pubKey VARCHAR(256)  NULL DEFAULT NULL COMMENT '证书tls公钥路径',
    cert_tls_priKey VARCHAR(256)  NULL DEFAULT NULL COMMENT '证书tls私钥路径',
    cert_type INT(4) NULL DEFAULT NULL COMMENT '证书类型',
    cert_status INT(4) NULL DEFAULT NULL COMMENT '证书状态',
    cluster_id VARCHAR(64) NULL DEFAULT NULL COMMENT '集群编号',
    org_id VARCHAR(64) NULL DEFAULT NULL COMMENT '组织编号',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bc_node;
CREATE TABLE bc_node
(
    id VARCHAR(64) NOT NULL COMMENT '主键ID',
    cluster_id VARCHAR(64) NULL DEFAULT NULL COMMENT '集群编号',
    org_id VARCHAR(64) NULL DEFAULT NULL COMMENT '组织编号',
    org_name VARCHAR(64) NULL DEFAULT NULL COMMENT '组织名称',
    node_type INT(4) NULL DEFAULT NULL COMMENT '节点类型',
    node_name VARCHAR(256)  NULL DEFAULT NULL COMMENT '节点名称',
    node_ip VARCHAR(64)  NULL DEFAULT NULL COMMENT '节点IP',
    node_port INT(11) NULL DEFAULT NULL COMMENT '节点端口',
    node_event_port INT(11)  NULL DEFAULT NULL COMMENT '节点事件监听端口',
    node_tls_path VARCHAR(256)  NULL DEFAULT NULL COMMENT '节点tls路径',
    create_time BIGINT(15)  NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bc_org;
CREATE TABLE bc_org
(
    id VARCHAR(64) NOT NULL COMMENT '主键ID',
    cluster_id VARCHAR(128) NULL DEFAULT NULL COMMENT '集群编号',
    cluster_name VARCHAR(128) NULL DEFAULT NULL COMMENT '集群名称',
    org_name VARCHAR(32) NULL DEFAULT NULL COMMENT '组织名称',
    org_msp_id VARCHAR(32) NULL DEFAULT NULL COMMENT '组织MSP',
    org_is_tls INT(4) NULL DEFAULT NULL COMMENT '是否开启TLS',
    org_status INT(4) NULL DEFAULT NULL COMMENT '组织状态',
    org_type INT(4)  NULL DEFAULT NULL COMMENT '组织类型',
    create_time BIGINT(15)  NULL DEFAULT NULL COMMENT '创建时间',
    cert_id VARCHAR(256)  NULL DEFAULT NULL COMMENT '证书编号',
    PRIMARY KEY (id)
);


DROP TABLE IF EXISTS bc_cluster_info;
CREATE TABLE bc_cluster_info
(
    cluster_id VARCHAR(64) NULL DEFAULT NULL COMMENT '集群编号',
    orderer_count INT(4)  NULL DEFAULT NULL COMMENT 'orderer节点总数',
    cluster_consensus_type INT(4) NULL DEFAULT NULL COMMENT '共识构建类型',
    cluster_version VARCHAR(32) NULL DEFAULT NULL COMMENT '集群版本',
    PRIMARY KEY (cluster_id)
);

DROP TABLE IF EXISTS bc_channel;
CREATE TABLE bc_channel
(
    id VARCHAR(64) NULL DEFAULT NULL COMMENT '通道编号',
    cluster_id VARCHAR(32) NULL DEFAULT NULL COMMENT '集群编号',
    cluster_name VARCHAR(32) NULL DEFAULT NULL COMMENT '集群名称',
    channel_name VARCHAR(32) NULL DEFAULT NULL COMMENT '通道名称',
    channel_status INT(4)  NULL DEFAULT NULL COMMENT '通道状态',
    is_block_listener INT(4) NULL DEFAULT NULL COMMENT '是否开启区块监听',
    PRIMARY KEY (id)
);

DROP TABLE IF EXISTS bc_channel_org;
CREATE TABLE bc_channel_org
(
    channel_id VARCHAR(64) NULL DEFAULT NULL COMMENT '通道编号',
    org_id VARCHAR(64) NULL DEFAULT NULL COMMENT '组织编号',
);

DROP TABLE IF EXISTS bc_channel_org_peer;
CREATE TABLE bc_channel_org_peer
(
    channel_id VARCHAR(64) NULL DEFAULT NULL COMMENT '通道编号',
    org_id VARCHAR(64) NULL DEFAULT NULL COMMENT '组织编号',
    peer_id VARCHAR(64) NULL DEFAULT NULL COMMENT '节点编号',
);