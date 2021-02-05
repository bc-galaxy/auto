#!/bin/bash

CLUSTER_NAME=$1
CHANNEL_NAME=$2
ORG_NAME=$3
ANCHOR_PEER_HOST=$4
ORDERER_ORG_NAME=$5
ORDERER_NAME=$6
ORDERER_PORT=$7
FABRIC_TOOLS_PATH=$8
FABRIC_OPERATE_SCRIPTS_PATH=$9
SAVE_CERTS_ROOT_PATH=${10}

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

export PATH=$PATH:$FABRIC_TOOLS_PATH

# running dir is project root dir
# for example:
# 1. update channel anchor peer
#   bash scripts/update-appchannel-anchor-peer.sh kledger-auto1 kscblockchain Xiaomi ordererhost:7050 example.com Orderer appchannel anchor_peer_host

CHANNEL_DATA_DIR="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/channels/$CHANNEL_NAME"
ORG_LOWER_CASE=$(echo $ORG_NAME | tr '[A-Z]' '[a-z]')
ORG_DIR="$CHANNEL_DATA_DIR/anchor/$ORG_LOWER_CASE"

if [ ! -d $ORG_DIR ]; then
   mkdir -p ${ORG_DIR}
fi

export CORE_PEER_LOCALMSPID=${ORDERER_ORG_NAME}MSP
export GODEBUG=netdns=go
export FABRIC_LOGGING_SPEC=info
export CORE_PEER_LOCALMSPTYPE=bccsp
export FABRIC_CFG_PATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/config
export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/msp
export ORDERER_CA=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/ca.crt

# get the latest config
if [ $TLS_ENABLED  = 'false' ]; then
    export CORE_PEER_TLS_ENABLED=false
    # log -n "peer channel fetch config ${ORG_DIR}/config_block.pb -o ${ordererUrl} -c ${channelName}"
    peer channel fetch config ${ORG_DIR}/config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:$ORDERER_PORT -c $CHANNEL_NAME
else
    export CORE_PEER_TLS_ENABLED=true
    # log -n "peer channel fetch config ${ORG_DIR}/config_block.pb -o ${ordererUrl} -c ${channelName} --tls --cafile ${ORDERER_CA}"
    peer channel fetch config ${ORG_DIR}/config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:$ORDERER_PORT -c $CHANNEL_NAME --tls --cafile ${ORDERER_CA}
fi

configtxlator proto_decode --input ${ORG_DIR}/config_block.pb --type common.Block | jq .data.data[0].payload.data.config > ${ORG_DIR}/config.json
anchor_peer_host_got=$(cat ${ORG_DIR}/config.json | jq ".channel_group.groups.Application.groups.${ORG_NAME}MSP.values.AnchorPeers.value.anchor_peers[0].host" | sed 's/"//g')
if [ ${ANCHOR_PEER_HOST} = ${anchor_peer_host_got} ]; then
    exit 0
fi
jq -s ".[0] * {\"channel_group\":{\"groups\":{\"Application\":{\"groups\":{\"${ORG_NAME}MSP\":{\"values\":{\"AnchorPeers\":.[1]}}}}}}}" ${ORG_DIR}/config.json ${ORG_DIR}/${ORG_LOWER_CASE}_anchor.json > ${ORG_DIR}/modified_config.json
configtxlator proto_encode --input ${ORG_DIR}/config.json --type common.Config --output ${ORG_DIR}/config.pb
configtxlator proto_encode --input ${ORG_DIR}/modified_config.json --type common.Config --output ${ORG_DIR}/modified_config.pb
configtxlator compute_update --channel_id $CHANNEL_NAME --original ${ORG_DIR}/config.pb --updated ${ORG_DIR}/modified_config.pb --output ${ORG_DIR}/${ORG_LOWER_CASE}_update.pb
configtxlator proto_decode --input ${ORG_DIR}/${ORG_LOWER_CASE}_update.pb --type common.ConfigUpdate | jq . > ${ORG_DIR}/${ORG_LOWER_CASE}_update.json
echo '{"payload":{"header":{"channel_header":{"channel_id":"'$CHANNEL_NAME'", "type":2}},"data":{"config_update":'$(cat ${ORG_DIR}/${ORG_LOWER_CASE}_update.json)'}}}' | jq . > ${ORG_DIR}/${ORG_LOWER_CASE}_update_in_envelope.json
configtxlator proto_encode --input ${ORG_DIR}/${ORG_LOWER_CASE}_update_in_envelope.json --type common.Envelope --output ${ORG_DIR}/${ORG_LOWER_CASE}_update_in_envelope.pb


export CORE_PEER_LOCALMSPID=${ORG_NAME}MSP
export FABRIC_CFG_PATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/config
export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/users/Admin@$ORG_LOWER_CASE-$CLUSTER_NAME/msp

peer channel signconfigtx -f ${ORG_DIR}/${ORG_LOWER_CASE}_update_in_envelope.pb

# update
if [ $TLS_ENABLED  = 'false' ]; then
    export CORE_PEER_TLS_ENABLED=false
    peer channel update -f ${ORG_DIR}/${ORG_LOWER_CASE}_update_in_envelope.pb -c $CHANNEL_NAME -o $ORDERER_NAME.$CLUSTER_NAME:$ORDERER_PORT
else
    export CORE_PEER_TLS_ENABLED=true
    peer channel update -f ${ORG_DIR}/${ORG_LOWER_CASE}_update_in_envelope.pb -c $CHANNEL_NAME -o $ORDERER_NAME.$CLUSTER_NAME:$ORDERER_PORT --tls --cafile $ORDERER_CA
fi

# check if success
export CORE_PEER_LOCALMSPID=${ORDERER_ORG_NAME}MSP
export GODEBUG=netdns=go
export FABRIC_LOGGING_SPEC=info
export CORE_PEER_LOCALMSPTYPE=bccsp
export FABRIC_CFG_PATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/config
export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/msp
export ORDERER_CA=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/ca.crt
count=1
while true
do
    # get the latest config
    if [ $TLS_ENABLED  = 'false' ]; then
        export CORE_PEER_TLS_ENABLED=false
        # log -n "peer channel fetch config ${ORG_DIR}/config_block.pb -o ${ordererUrl} -c ${channelName}"
        peer channel fetch config ${ORG_DIR}/config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:$ORDERER_PORT -c $CHANNEL_NAME
    else
        export CORE_PEER_TLS_ENABLED=true
        # log -n "peer channel fetch config ${ORG_DIR}/config_block.pb -o ${ordererUrl} -c ${channelName} --tls --cafile ${ORDERER_CA}"
        peer channel fetch config ${ORG_DIR}/config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:$ORDERER_PORT -c $CHANNEL_NAME --tls --cafile ${ORDERER_CA}
    fi
    # check add update anchor peer if success. 
    configtxlator proto_decode --input ${ORG_DIR}/config_block.pb --type common.Block | jq .data.data[0].payload.data.config > ${ORG_DIR}/config.json
    anchor_peer_host_got=$(cat ${ORG_DIR}/config.json | jq ".channel_group.groups.Application.groups.${ORG_NAME}MSP.values.AnchorPeers.value.anchor_peers[0].host" | sed 's/"//g')
    if [ ${ANCHOR_PEER_HOST} = ${anchor_peer_host_got} ]; then
        break
    fi
    count=$((count + 1))
    # check 50 times
    if [ ${count} -gt 50 ]; then
        log -n "add ${ORG_NAME} to channel:$CHANNEL_NAME failed"
        exit 1
    fi
done
