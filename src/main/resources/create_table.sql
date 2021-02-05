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
    PRIMARY KEY (id)
);