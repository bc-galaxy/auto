#!/bin/bash

CLUSTER_NAME=$1
ORG_NAME=$2
ORDERER_ORG_NAME=$3
ORDERER_NAME=$4
FABRIC_TOOLS_PATH=$5
FABRIC_OPERATE_SCRIPTS_PATH=$6
SAVE_CERTS_ROOT_PATH=$7

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

export PATH=$PATH:$FABRIC_TOOLS_PATH

ORG_LOWER_CASE=$(echo ${ORG_NAME} | tr '[A-Z]' '[a-z]')
ORG_DIR="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/channels/$CLUSTER_NAME/$ORG_LOWER_CASE"

if [ ! -d $ORG_DIR ]; then
   mkdir -p $ORG_DIR
fi

configtxgen -configPath $ORG_DIR -printOrg ${ORG_NAME}MSP > $ORG_DIR/sys_${ORG_LOWER_CASE}.json

export CORE_PEER_LOCALMSPID=${ORDERER_ORG_NAME}MSP
export GODEBUG=netdns=go
export FABRIC_LOGGING_SPEC=info
export CORE_PEER_LOCALMSPTYPE=bccsp
export FABRIC_CFG_PATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/config
export CORE_PEER_MSPCONFIGPATH=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/msp
export ORDERER_CA=$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/ca.crt
# fetch latest config
if [ $TLS_ENABLED  = 'false' ]; then
    export CORE_PEER_TLS_ENABLED=false
    echo peer channel fetch config $ORG_DIR/sys_config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CLUSTER_NAME
    peer channel fetch config $ORG_DIR/sys_config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CLUSTER_NAME
else
    export CORE_PEER_TLS_ENABLED=true
    echo peer channel fetch config $ORG_DIR/sys_config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CLUSTER_NAME --tls --cafile ${ORDERER_CA}
    peer channel fetch config $ORG_DIR/sys_config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CLUSTER_NAME --tls --cafile ${ORDERER_CA}
fi

# parse config
configtxlator proto_decode --input $ORG_DIR/sys_config_block.pb --type common.Block | jq .data.data[0].payload.data.config > $ORG_DIR/sys_config.json
getOrgMSP=$(cat $ORG_DIR/sys_config.json | jq ".channel_group.groups.Consortiums.groups.SampleConsortium.groups.${ORG_NAME}MSP.values.MSP.value.config.name" | sed 's/"//g')
if [ ${getOrgMSP} = "${ORG_NAME}MSP" ]; then
    exit 0
fi
jq -s ".[0] * {\"channel_group\":{\"groups\":{\"Consortiums\":{\"groups\":{\"SampleConsortium\":{\"groups\":{\"${ORG_NAME}MSP\":.[1]}}}}}}}" $ORG_DIR/sys_config.json $ORG_DIR/sys_${ORG_LOWER_CASE}.json > $ORG_DIR/sys_modified_config.json
configtxlator proto_encode --input $ORG_DIR/sys_config.json --type common.Config --output $ORG_DIR/sys_config.pb
configtxlator proto_encode --input $ORG_DIR/sys_modified_config.json --type common.Config --output $ORG_DIR/sys_modified_config.pb
configtxlator compute_update --channel_id $CLUSTER_NAME --original $ORG_DIR/sys_config.pb --updated $ORG_DIR/sys_modified_config.pb --output $ORG_DIR/sys_${ORG_LOWER_CASE}_update.pb
configtxlator proto_decode --input $ORG_DIR/sys_${ORG_LOWER_CASE}_update.pb --type common.ConfigUpdate | jq . > $ORG_DIR/sys_${ORG_LOWER_CASE}_update.json
echo '{"payload":{"header":{"channel_header":{"channel_id":"'$CLUSTER_NAME'", "type":2}},"data":{"config_update":'$(cat $ORG_DIR/sys_${ORG_LOWER_CASE}_update.json)'}}}' | jq . > $ORG_DIR/sys_${ORG_LOWER_CASE}_update_in_envelope.json
configtxlator proto_encode --input $ORG_DIR/sys_${ORG_LOWER_CASE}_update_in_envelope.json --type common.Envelope --output $ORG_DIR/sys_${ORG_LOWER_CASE}_update_in_envelope.pb

# get sign
peer channel signconfigtx -f $ORG_DIR/sys_${ORG_LOWER_CASE}_update_in_envelope.pb

# update
if [ $TLS_ENABLED  = 'false' ]; then
    export CORE_PEER_TLS_ENABLED=false
    peer channel update -f $ORG_DIR/sys_${ORG_LOWER_CASE}_update_in_envelope.pb -c $CLUSTER_NAME -o $ORDERER_NAME.$CLUSTER_NAME:7050
else
    export CORE_PEER_TLS_ENABLED=true
    peer channel update -f $ORG_DIR/sys_${ORG_LOWER_CASE}_update_in_envelope.pb -c $CLUSTER_NAME -o $ORDERER_NAME.$CLUSTER_NAME:7050 --tls --cafile $ORDERER_CA
fi

# check if add success check max 50 times
count=1
while true
do
    if [ $TLS_ENABLED  = 'false' ]; then
        export CORE_PEER_TLS_ENABLED=false
        peer channel fetch config $ORG_DIR/sys_config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CLUSTER_NAME
    else
        export CORE_PEER_TLS_ENABLED=true
        peer channel fetch config $ORG_DIR/sys_config_block.pb -o $ORDERER_NAME.$CLUSTER_NAME:7050 -c $CLUSTER_NAME --tls --cafile ${ORDERER_CA}
    fi

    # parse config
    configtxlator proto_decode --input $ORG_DIR/sys_config_block.pb --type common.Block | jq .data.data[0].payload.data.config > $ORG_DIR/sys_config.json
    getOrgMSP=$(cat $ORG_DIR/sys_config.json | jq ".channel_group.groups.Consortiums.groups.SampleConsortium.groups.${ORG_NAME}MSP.values.MSP.value.config.name" | sed 's/"//g')
    if [ ${getOrgMSP} = "${ORG_NAME}MSP" ]; then
        log -n "add $ORG_NAME to channel:$CLUSTER_NAME success."
        exit 0
    fi

    count=$((count + 1))
    # check 50 times
    if [ ${count} -gt 50 ]; then
        log -n "add $ORG_NAME to channel:$CLUSTER_NAME failed"
        exit 1
    fi
done