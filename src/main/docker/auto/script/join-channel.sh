#!/bin/bash

CLUSTER_NAME=$1
CHANNEL_NAME=$2
ORG_NAME=$3
PEER_NAME=$4
PEER_PORT=$5
ORDERER_ORG_NAME=$6
ORDERER_NAME=$7
FABRIC_TOOLS_PATH=$8
FABRIC_OPERATE_SCRIPTS_PATH=$9
SAVE_CERTS_ROOT_PATH=${10}

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

export PATH=$PATH:$FABRIC_TOOLS_PATH

CHANNEL_DATA_DIR="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/channels/$CHANNEL_NAME"
ORG_LOWER_CASE=$(echo ${ORG_NAME} | tr '[A-Z]' '[a-z]')

fetch_channel(){
    export CORE_PEER_LOCALMSPID=${ORDERER_ORG_NAME}MSP
    export FABRIC_CFG_PATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/config
    export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/msp
    export ORDERER_CA=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/ca.crt
    if [ $TLS_ENABLED  = 'false' ]; then
        peer channel fetch 0 $CHANNEL_DATA_DIR/$CHANNEL_NAME.block -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CHANNEL_NAME
    else
        peer channel fetch 0 $CHANNEL_DATA_DIR/$CHANNEL_NAME.block -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CHANNEL_NAME --tls --cafile $ORDERER_CA
    fi
}

join_channel() {
    fetch_channel

    export CORE_PEER_LOCALMSPID=${ORG_NAME}MSP
    export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/users/Admin@$ORG_LOWER_CASE-$CLUSTER_NAME/msp
    export CORE_PEER_ADDRESS=$PEER_NAME.$CLUSTER_NAME:$PEER_PORT

    if [ $TLS_ENABLED  = 'false' ]; then
        export CORE_PEER_TLS_ENABLED=false
    else
        export CORE_PEER_TLS_ENABLED=true
        export CORE_PEER_TLS_ROOTCERT_FILE=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/peers/$PEER_NAME.$CLUSTER_NAME/tls/ca.crt
        export CORE_PEER_TLS_CERT_FILE=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/peers/$PEER_NAME.$CLUSTER_NAME/tls/client.crt
        export CORE_PEER_TLS_KEY_FILE=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/peers/$PEER_NAME.$CLUSTER_NAME/tls/client.key
    fi

    result=$(peer channel list)
    for var in $result
    do
        if [ "${var}" = b"$CHANNEL_NAME" ]; then
            log -n "$PEER_NAME has joined channel $CHANNEL_NAME ."
            exit 0
        fi
    done
    peer channel join -b $CHANNEL_DATA_DIR/$CHANNEL_NAME.block
    log -n "$PEER_NAME join channel $CHANNEL_NAME successfully."
}

echo "Peer $PEER_NAME start joining channel..."
join_channel