#!/bin/bash

CLUSTER_NAME=$1
CHANNEL_NAME=$2
ORG_NAME=$3
ORDERER_NAME=$4
FABRIC_TOOLS_PATH=$5
FABRIC_OPERATE_SCRIPTS_PATH=$6
SAVE_CERTS_ROOT_PATH=$7

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

export PATH=$PATH:$FABRIC_TOOLS_PATH

CHANNEL_DATA_DIR="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/channels/$CHANNEL_NAME"
ORG_LOWER_CASE=$(echo ${ORG_NAME} | tr '[A-Z]' '[a-z]')

generate_channel_tx() {
    echo
    echo "#################################################################"
    echo "### Generating channel configuration transaction 'channel.tx' ###"
    echo "#################################################################"
    configtxgen -configPath $CHANNEL_DATA_DIR -profile TwoOrgsChannel -channelID $CHANNEL_NAME -outputCreateChannelTx $CHANNEL_DATA_DIR/channel.tx
}

create_channel() {
    export CORE_PEER_LOCALMSPID=${ORG_NAME}MSP
    export FABRIC_CFG_PATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/config
    export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/users/Admin@$ORG_LOWER_CASE-$CLUSTER_NAME/msp
    export ORDERER_CA=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/ca.crt
    if [ $TLS_ENABLED  = 'false' ]; then
        peer channel create -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CHANNEL_NAME -f $CHANNEL_DATA_DIR/channel.tx
    else
        export CORE_PEER_TLS_ENABLED=true
        peer channel create -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CHANNEL_NAME -f  $CHANNEL_DATA_DIR/channel.tx --tls --cafile $ORDERER_CA
    fi

    # mv genesis block to channel dir
    mv $CHANNEL_NAME.block  $CHANNEL_DATA_DIR
    log -n "create channel:$CHANNEL_NAME success."
}

echo "Generating channel.tx for $CHANNEL_NAME..."
generate_channel_tx

echo "Create channel $CHANNEL_NAME..."
create_channel