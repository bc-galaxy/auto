#!/bin/bash
# ========= parameter list ===============
# 1. OPERATE_TYPE="peerOrg" or "peer"
# 2. CLUSTER_NAME="ksc-blockchain"
# 3. PEER="peer0" if the type is "peerOrg" this para not use, must input a value
# 4. USER_PWD="admin:adminpw"
# 5. CA_URL="msp.intermediate.ca1:7054"

# ========================================
OPERATE_TYPE=$1
CLUSTER_NAME=$2
ORG_NAME=$(echo ${3} | tr '[A-Z]' '[a-z]')
PEER=$4
USER_PWD=$5
CA_URL=$6
FABRIC_OPERATE_SCRIPTS_PATH=$7
FABRIC_CA_CLIENT_MSP_PATH=$8
GENERATE_CERTS_ROOT_PATH=$9
SAVE_CERTS_ROOT_PATH=${10}

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

# fabric-ca-client directory
export PATH=$PATH:$FABRIC_CA_CLIENT_MSP_PATH

# running dir is project root dir
# for example:
# 1. Generate peer org msp certs
#   bash scripts/generate-peer-msp-certs.sh peerOrg kledger-auto ksc-blockchain peer0 org1.example.com com.example.org1 admin1:adminpw msp.intermediate.ca1:7054
# 2. Generate peer msp certs
#   bash scripts/generate-peer-msp-certs.sh peer kledger-auto ksc-blockchain peer0 org1.example.com com.example.org1 admin1:adminpw msp.intermediate.ca1:7054
#

# peer orgs certs root path
ROOTPATH="$GENERATE_CERTS_ROOT_PATH/$CLUSTER_NAME/certs/msp/peerOrgs"

# Path format
SYS_ADMIN_PATH=$ROOTPATH/admin
DOMAIN_MSP_PATH=$ROOTPATH/$ORG_NAME-$CLUSTER_NAME/msp
ADMIN_MSP_PATH=$ROOTPATH/$ORG_NAME-$CLUSTER_NAME/users/Admin@$ORG_NAME-$CLUSTER_NAME/msp
PEER_MSP_PATH=$ROOTPATH/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME/msp

enroll_ca_admin() {
   log -n "Enroll system admin ..."
   rm -rf $ROOTPATH/admin
   fabric-ca-client enroll -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Enroll system admin" $?
   log -n "Enroll system admin done."
}

remove_affiliation() {
   log -n "Remove affiliation..."
   affiliation=$1
   log -n "Remove affiliation:$affiliation"
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation remove --force  $affiliation
   judge_operation "Remove affiliation" $?
   log -n "Remove affiliation done."
}

# affiliation list
affiliation_list() {
   log -n "Affiliation list..."
   cp -rf {CONFIG_PATH}/fabric-ca-client-config.yaml ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/caurl/$CA_URL/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation list
   judge_operation "Affiliation list" $?
   log -n "Affiliation list done."
}


add_affiliation() {
   log -n "Add affiliation..."
   affiliation=$1
   log -n "Add affiliation:$affiliation"
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation add $affiliation
   judge_operation "Add affiliation" $?
   log -n "Add affiliation done."
}

get_ca_cert() {
   fabric-ca-client getcacert -M ${DOMAIN_MSP_PATH} -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Get ca cert" $?
   rm -rf $DOMAIN_MSP_PATH/signcerts
   rm -rf $DOMAIN_MSP_PATH/keystore
   rm -rf $DOMAIN_MSP_PATH/user
   rm -rf $DOMAIN_MSP_PATH/Issuer*
   rm -rf $DOMAIN_MSP_PATH/cacerts/ca.$CLUSTER_NAME-cert.pem
   mv $DOMAIN_MSP_PATH/cacerts/*.pem $DOMAIN_MSP_PATH/cacerts/ca.$CLUSTER_NAME-cert.pem
   if [ -d "$DOMAIN_MSP_PATH/intermediatecerts" ]; then
      cp -rf $FABRIC_CA_CLIENT_MSP_PATH/config-i.yaml $DOMAIN_MSP_PATH/config.yaml
      sed -i "s/caname/ca.$CLUSTER_NAME-cert.pem/g" $DOMAIN_MSP_PATH/config.yaml
      sed -i "s/capath/intermediatecerts/g" $DOMAIN_MSP_PATH/config.yaml
      rm -rf ${DOMAIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      mv ${DOMAIN_MSP_PATH}/intermediatecerts/*.pem ${DOMAIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
   else
      cp -rf $FABRIC_CA_CLIENT_MSP_PATH/config-i.yaml $DOMAIN_MSP_PATH/config.yaml
      sed -i "s/caname/ca.$CLUSTER_NAME-cert.pem/g" $DOMAIN_MSP_PATH/config.yaml
      sed -i "s/capath/cacerts/g" $DOMAIN_MSP_PATH/config.yaml
   fi
}

register_admin_user() {
   log -n "Register admin user..."
   fabric-ca-client register --id.name Admin@$ORG_NAME-$CLUSTER_NAME --id.type client --id.affiliation "$CLUSTER_NAME" --id.secret=123456 \
      --id.attrs '"hf.Registrar.Roles=client,orderer,peer,user","hf.Registrar.DelegateRoles=client,orderer,peer,user",hf.Registrar.Attributes=*,hf.GenCRL=true,hf.Revoker=true,hf.AffiliationMgr=true,hf.IntermediateCA=true,role=admin:ecert' \
      -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Register admin user" $?
   log -n "Register admin user done."
}

enroll_admin_user() {
   log -n "Enroll admin user..."
   cp -rf $FABRIC_CA_CLIENT_MSP_PATH/fabric-ca-client-config.yaml ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/caurl/$CA_URL/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/cnname/Admin@$ORG_NAME-$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/domainstr/$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/ouname/client/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   fabric-ca-client enroll -u http://Admin@$ORG_NAME-$CLUSTER_NAME:123456@$CA_URL -H ${SYS_ADMIN_PATH} -M $ADMIN_MSP_PATH
   judge_operation "Enroll admin user" $?
   mkdir -p ${ADMIN_MSP_PATH}/admincerts/
   mkdir -p ${DOMAIN_MSP_PATH}/admincerts
   rm -rf ${ADMIN_MSP_PATH}/signcerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem
   mv ${ADMIN_MSP_PATH}/signcerts/*.pem  ${ADMIN_MSP_PATH}/signcerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem
   cp -rf ${ADMIN_MSP_PATH}/signcerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem ${ADMIN_MSP_PATH}/admincerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem
   cp -rf ${ADMIN_MSP_PATH}/signcerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem ${DOMAIN_MSP_PATH}/admincerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem
   
   rm -rf ${ADMIN_MSP_PATH}/user
   rm -rf ${ADMIN_MSP_PATH}/Issuer*
   
   rm -rf ${ADMIN_MSP_PATH}/cacerts/ca.$CLUSTER_NAME-cert.pem
   mv ${ADMIN_MSP_PATH}/cacerts/*.pem ${ADMIN_MSP_PATH}/cacerts/ca.$CLUSTER_NAME-cert.pem
   if [ -d "${ADMIN_MSP_PATH}/intermediatecerts" ]; then
      rm -rf  ${ADMIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      mv ${ADMIN_MSP_PATH}/intermediatecerts/*.pem ${ADMIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      rm -rf ${ADMIN_MSP_PATH}/cacerts.bak
      mv ${ADMIN_MSP_PATH}/cacerts ${ADMIN_MSP_PATH}/cacerts.bak
      cp -rf ${ADMIN_MSP_PATH}/intermediatecerts ${ADMIN_MSP_PATH}/cacerts
   fi
   log -n "Enroll admin user done."
}

register_peer() {
   log -n "Register peer..."
   fabric-ca-client register -H ${SYS_ADMIN_PATH} --id.secret=123456 --id.name $PEER.$CLUSTER_NAME --id.type peer \
      --id.affiliation "$CLUSTER_NAME" -u http://$USER_PWD@$CA_URL
   judge_operation "Register peer" $?
   log -n "Register peer done."
}

enroll_peer() {
   log -n "Enroll peer..."
   cp -rf $FABRIC_CA_CLIENT_MSP_PATH/fabric-ca-client-config.yaml ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sleep 1
   sed -i "s/caurl/$CA_URL/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/cnname/$PEER.$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/domainstr/$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/ouname/peer/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   fabric-ca-client enroll -u http://$PEER.$CLUSTER_NAME:123456@$CA_URL -H ${SYS_ADMIN_PATH} -M ${PEER_MSP_PATH}
   judge_operation "Enroll peer" $?
   
   mkdir -p ${PEER_MSP_PATH}/admincerts
   cp -rf ${ADMIN_MSP_PATH}/signcerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem ${PEER_MSP_PATH}/admincerts/Admin@$ORG_NAME-$CLUSTER_NAME-cert.pem
   rm -rf ${PEER_MSP_PATH}/user
   rm -rf ${PEER_MSP_PATH}/Issuer*
   rm -rf ${PEER_MSP_PATH}/signcerts/$PEER.$CLUSTER_NAME-cert.pem
   mv ${PEER_MSP_PATH}/signcerts/*.pem ${PEER_MSP_PATH}/signcerts/$PEER.$CLUSTER_NAME-cert.pem
   rm -rf ${PEER_MSP_PATH}/cacerts/ca.$CLUSTER_NAME-cert.pem
   mv ${PEER_MSP_PATH}/cacerts/*.pem ${PEER_MSP_PATH}/cacerts/ca.$CLUSTER_NAME-cert.pem
   if [ -d ${PEER_MSP_PATH}/intermediatecerts ]; then
      cp -rf $FABRIC_CA_CLIENT_MSP_PATH/config-i.yaml ${PEER_MSP_PATH}/config.yaml
      sleep 1
      sed -i "s/caname/ca.$CLUSTER_NAME-cert.pem/g" ${PEER_MSP_PATH}/config.yaml
      sed -i "s/capath/intermediatecerts/g" ${PEER_MSP_PATH}/config.yaml
      rm -rf ${PEER_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      mv ${PEER_MSP_PATH}/intermediatecerts/*.pem ${PEER_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
   else
      cp -rf $FABRIC_CA_CLIENT_MSP_PATH/config-i.yaml ${PEER_MSP_PATH}/config.yaml
      sleep 1
      sed -i "s/caname/ca.$CLUSTER_NAME-cert.pem/g" ${PEER_MSP_PATH}/config.yaml
      sed -i "s/capath/cacerts/g" ${PEER_MSP_PATH}/config.yaml
   fi
   log -n "Enroll peer done."
}

copy_certs_to_crypto_config_dir() {
   CRYPTO_PATH="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations"
   if [ ! -d $CRYPTO_PATH ]; then
      mkdir -p ${CRYPTO_PATH}
   fi
   cp -rf $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/ $CRYPTO_PATH
}

# # start from here.
# if [ $OPERATE_TYPE = "peerOrg" ]; then
#    log -n "Generate peer org msp certs..."
#    enroll_ca_admin
#    affiliation_list
#    add_affiliation $DOMAIN_REVERSE
#    affiliation_list
#    get_ca_cert
#    register_admin_user
#    enroll_admin_user
#    copy_certs_to_crypto_config_dir
#    log -n "Generate peer org msp certs done."
# elif [ $OPERATE_TYPE = "peer" ]; then
#    log -n "Generate peer msp certs..."
#    register_peer
#    enroll_peer
#    copy_certs_to_crypto_config_dir
#    log -n "Generate peer msp certs done."
# else
#    log -n "Operate type error."
#    exit 1
# fi

# start from here
if [ $OPERATE_TYPE = "add_affiliation" ]; then
   enroll_ca_admin
   add_affiliation $CLUSTER_NAME
elif [ $OPERATE_TYPE = "register_admin_user" ]; then
   enroll_ca_admin
   get_ca_cert
   register_admin_user
elif [ $OPERATE_TYPE = "enroll_admin_user" ]; then
   enroll_admin_user
   copy_certs_to_crypto_config_dir
elif [ $OPERATE_TYPE = "register_peer" ]; then
   register_peer
elif [ $OPERATE_TYPE = "enroll_peer" ]; then
   enroll_peer
   copy_certs_to_crypto_config_dir
else
   log -n "Operate type error."
   exit 1
fi