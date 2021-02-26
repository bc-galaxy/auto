package org.bc.auto.config;

public class BlockChainFabricConstructConstant {

    public static final String GOLANG = "GOLANG";
    public static final String JAVA = "JAVA";
    public static final String NODE = "NODE";

    public static final String HYPERLEDGER_FABRIC = "HyperLedgerFabric";
    public static final String METHOD = "method";
    public static final String RESULT = "result";
    public static final String ENVELOPES = "envelopes";

    public static final int SUCCESS = 200;
    public static final int ERROR = 500;

    public static final String APP_NAME = "kscbc-auto-cluster";

    public static final String STORAGE_CLASS_NAME = "managed-nfs-storage";

    public static final int PDB_MAX_UNAVAILABLE = 1;

    public static final String KAFKA_NAME = "kafka";
    public static final String KAFKA_IMAGE = "hyperledger/fabric-kafka:0.4.18";
    //    public static final String KAFKA_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-kafka:0.4.16";
    public static final String KAFKA_IMAGE_SINGLE = "reg.ksbc.com/support/kafka:2.12-2.5.0";
    public static final String KAFKA_IMAGE_CLUSTER = "reg.ksbc.com/support/kafka:2.2.0";
    public static final int KAFKA_NUM = 4;
    public static final int KAFKA_PORT = 9092;

    public static final String ZOO_KEEPER_NAME = "zookeeper";
    public static final String ZOO_KEEPER_IMAGE = "hyperledger/fabric-zookeeper:0.4.18";
    //    public static final String ZOO_KEEPER_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-zookeeper:0.4.18";
    public static final String ZOO_KEEPER_IMAGE_SINGLE = "reg.ksbc.com/support/zookeeper:3.5";
    public static final String ZOO_KEEPER_IMAGE_CLUSTER = "reg.ksbc.com/support/zookeeper:3.4.10";
    public static final int ZOO_KEEPER_NUM = 3;
    public static final int ZOO_KEEPER_CLIENT_PORT = 2181;
    public static final int ZOO_KEEPER_SERVER_PORT = 2888;
    public static final int ZOO_KEEPER_LEADER_ELECTION_PORT = 3888;

    public static final String MSP_CA_NAME = "msp-root-ca";
    public static final String MSP_CA_IMAGE = "hyperledger/fabric-ca-ml:1.4.5";
//    public static final String MSP_CA_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-ca-sm:1.4.0.1";

    public static final int CA_PORT = 7054;

    public static final String TLS_CA_NAME = "tls-root-ca";
    public static final String TLS_CA_IMAGE = "hyperledger/fabric-ca-ml:1.4.5";
//    public static final String TLS_CA_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-ca:1.4.0.1";

    public static final String ORDERER_ORG_NAME = "Orderer";
    public static final String ORDERER_IMAGE = "hyperledger/fabric-orderer:1.4.5";
    //    public static final String ORDERER_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-orderer:1.4.5";
    public static final int ORDERER_PORT = 7050;
    public static final int ORDERER_MONITOR_PORT = 8443;

    public static final String COUCH_DB_IMAGE = "hyperledger/fabric-couchdb:0.4.18";
    //    public static final String COUCH_DB_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-couchdb:0.4.18";
    public static final int COUCH_DB_PORT = 5984;
    public static final String COUCH_DB_USERNAME = "kscbc-auto-cluster-username";
    public static final String COUCH_DB_PASSWORD = "kscbc-auto-cluster-password";

    public static final String PEER_IMAGE = "hyperledger/fabric-peer:1.4.5";
    //    public static final String PEER_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-peer:1.4.5";
    public static final int PEER_PORT = 7051;
    public static final int PEER_CHAINCODE_PORT = 7052;

    public static final String CLI_IMAGE = "hyperledger/fabric-tools:1.4.5";
//    public static final String CLI_IMAGE = "reg.ksbc.com/kledger/hyperledger/fabric-tools:1.4.5";

    public static final String ROOT_CA_LOGIN_INFO = "admin:adminpw";
    public static final String INTERMEDIATE_CA_LOGIN_INFO = "admin1:adminpw";

    public static final String COMMON_OPERATE_SCRIPT_NAME = "common-operate.sh";
    public static final String GENERATE_GENESIS_SCRIPT_NAME = "generate-genesis.sh";
    public static final String CREATE_CHANNEL_SCRIPT_NAME = "create-channel.sh";
    public static final String JOIN_CHANNEL_SCRIPT_NAME = "join-channel.sh";
    public static final String ADD_ORG_TO_SYS_CHANNEL = "add-org-to-syschannel.sh";
    public static final String UPDATE_APP_CHANNEL_ANCHOR_PEER = "update-appchannel-anchor-peer.sh";
    public static final String ORDERER_MSP_SCRIPT_NAME = "generate-orderer-msp-certs.sh";
    public static final String ORDERER_TLS_SCRIPT_NAME = "generate-orderer-tls-certs.sh";
    public static final String PEER_MSP_SCRIPT_NAME = "generate-peer-msp-certs.sh";
    public static final String PEER_TLS_SCRIPT_NAME = "generate-peer-tls-certs.sh";

}
