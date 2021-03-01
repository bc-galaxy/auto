#!/bin/bash

OPERATE_TYPE=$1
CLUSTER_NAME=$2
FABRIC_CONFIG_PATH=$3
FABRIC_OPERATE_SCRIPTS_PATH=$4
SAVE_CERTS_ROOT_PATH=$5
GENERATE_CERTS_ROOT_PATH=$6

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

copy_config_yaml() {
    cp $FABRIC_CONFIG_PATH $SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME -r
}

clean_cluster_data() {
    rm -rf $GENERATE_CERTS_ROOT_PATH/$CLUSTER_NAME
    rm -rf $SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME
}

# start from here
if [ $OPERATE_TYPE = "copy_config" ]; then
    echo "Copy orderer.yaml and core.yaml to pv storage dir"
    copy_config_yaml
elif [ $OPERATE_TYPE = "clean_data" ]; then
    echo "Clean cluster data from pv storage dir"
    clean_cluster_data
else
    log -n "Operate type error."
    exit 1
fi