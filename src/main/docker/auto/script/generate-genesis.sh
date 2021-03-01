#!/bin/bash

CLUSTER_NAME=$1
CHANNEL_NAME=$2
FABRIC_TOOLS_PATH=$3
FABRIC_OPERATE_SCRIPTS_PATH=$4
SAVE_CERTS_ROOT_PATH=$5

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

export PATH=$PATH:$FABRIC_TOOLS_PATH

CHANNEL_DATA_DIR="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/channels/$CHANNEL_NAME"

generate_genesis_block() {
    echo "##########################################################"
    echo "#########  Generating Orderer Genesis block ##############"
    echo "##########################################################"
    # Note: For some unknown reason (at least for now) the block file can't be
    # named orderer.genesis.block or the orderer will fail to launch!
    if [ ! -d $CHANNEL_DATA_DIR ]; then
       mkdir -p $CHANNEL_DATA_DIR
    fi
    # generate gensis block
    configtxgen -configPath $CHANNEL_DATA_DIR -profile TwoOrgsOrdererGenesis -channelID $CHANNEL_NAME -outputBlock $CHANNEL_DATA_DIR/../genesis.block
}

echo "Generate genesis block..."
generate_genesis_block