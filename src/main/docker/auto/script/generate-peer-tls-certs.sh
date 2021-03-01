#!/bin/bash
#==================主要功能===================
#签发管理员证书
#注册组织
#添加com
#添加联盟.com
#注册联盟管理员（Admin@example.com）
#注册peer.$CLUSTER_NAME
#签发peer组织根证书 getcacert
#签发 peer tls证书
#============================================

# ========= parameter list ===============
# 1. OPERATE_TYPE="peerOrg" or "peer"
# 2. CLUSTER_NAME="ksc-blockchain"
# 3. PEER="peer0" if the type is "peerOrg" this para not use, must input a value
# 4. USER_PWD="admin:adminpw"
# 5. CA_URL="tls.intermediate.ca1:7054"

# ========================================

OPERATE_TYPE=$1
CLUSTER_NAME=$2
ORG_NAME=$(echo ${3} | tr '[A-Z]' '[a-z]')
PEER=$4
USER_PWD=$5
CA_URL=$6
FABRIC_OPERATE_SCRIPTS_PATH=$7
FABRIC_CA_CLIENT_TLS_PATH=$8
GENERATE_CERTS_ROOT_PATH=$9
SAVE_CERTS_ROOT_PATH=${10}

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

# fabric-ca-client directory
export PATH=$PATH:$FABRIC_CA_CLIENT_TLS_PATH

# running dir is project root dir
# for example:
# 1. Generate peer org tls certs
#   bash scripts/generate-peer-tls-certs.sh peerOrg kledger-auto ksc-blockchain peer0 org1.example.com com.example.org1 admin1:adminpw msp.intermediate.ca1:7054
# 2. Generate peer tls certs
#   bash scripts/generate-peer-tls-certs.sh peer kledger-auto ksc-blockchain peer0 org1.example.com com.example.org1 admin1:adminpw msp.intermediate.ca1:7054
#

# peer orgs certs root path
ROOTPATH="$GENERATE_CERTS_ROOT_PATH/$CLUSTER_NAME/certs/tls/peerOrgs"
CRYPTO_PATH="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/peerOrganizations"

enroll_ca_admin() {
   log -n "enroll system admin ..."
   rm -rf $ROOTPATH/admin
   fabric-ca-client enroll -u http://$USER_PWD@$CA_URL -H $ROOTPATH/admin
   sleep 1
   judge_operation "Enroll system admin" $?
   log -n "enroll system admin done."
}

remove_affiliation() {
   log -n "Remove affiliation..."
   affiliation=$1
   log -n "Remove affiliation:$affiliation"
   fabric-ca-client -H $ROOTPATH/admin affiliation remove --force  $affiliation
   judge_operation "Remove affiliation" $?
}

# affiliation list
affiliation_list() {
   log -n "Affiliation list..."
   fabric-ca-client -H $ROOTPATH/admin affiliation list
   judge_operation "Affiliation list" $?
   log -n "Affiliation list done."
}

add_affiliation() {
   log -n "Add affiliation..."
   affiliation=$1
   log -n "Add affiliation:$affiliation"
   fabric-ca-client -H $ROOTPATH/admin affiliation add $affiliation
   judge_operation "Add affiliation" $?
   log -n "Add affiliation done."
}

get_ca_cert() {
   rm -rf $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/tls
   fabric-ca-client getcacert -M $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/tls -u http://$USER_PWD@$CA_URL -H $ROOTPATH/admin
   judge_operation "Get ca cert" $?

   MSP_TLSCACERTS=T${CRYPTO_PAH}/$ORG_NAME-$CLUSTER_NAME/msp/tlscacerts
   MSP_TLSICACERTS=${CRYPTO_PATH}/$ORG_NAME-$CLUSTER_NAME/msp/tlsintermediatecerts
   CATLS_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/tls/cacerts
   ICATLS_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/tls/intermediatecerts

   mkdir -p ${MSP_TLSCACERTS}
   cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
   if [ -d "$ROOTPATH/$ORG_NAME-$CLUSTER_NAME/tls/intermediatecerts" ]; then
      mkdir -p ${MSP_TLSICACERTS}
      cp -rf ${ICATLS_PATH}/*.pem $MSP_TLSICACERTS/tlsica.$CLUSTER_NAME-cert.pem
   fi
}

register_admin_user() {
   log -n "Register admin user..."
   fabric-ca-client register --id.name Admin@$ORG_NAME-$CLUSTER_NAME  --id.affiliation "$CLUSTER_NAME" --id.secret=123456 \
      --id.attrs '"hf.Registrar.Roles=client,orderer,peer,user","hf.Registrar.DelegateRoles=client,orderer,peer,user",hf.Registrar.Attributes=*,hf.GenCRL=true,hf.Revoker=true,hf.AffiliationMgr=true,hf.IntermediateCA=true,role=admin:ecert' \
      -u http://$USER_PWD@$CA_URL -H $ROOTPATH/admin
   judge_operation "Register admin user" $?
   log -n "Register admin user done."
}

enroll_admin_user() {
   rm -rf $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/users/Admin@$ORG_NAME-$CLUSTER_NAME/tls
   fabric-ca-client enroll --enrollment.profile tls -u http://Admin@$ORG_NAME-$CLUSTER_NAME:123456@$CA_URL \
      -H $ROOTPATH/admin --csr.cn=$CLUSTER_NAME --csr.hosts=["$CLUSTER_NAME"] \
      -M $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/users/Admin@$ORG_NAME-$CLUSTER_NAME/tls
   judge_operation "Enroll admin user" $?

   TLSCERT_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/users/Admin@$ORG_NAME-$CLUSTER_NAME/tls/signcerts
   TLSKEY_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/users/Admin@$ORG_NAME-$CLUSTER_NAME/tls/keystore

   ADMIN_PATH=${CRYPTO_PATH}/$ORG_NAME-$CLUSTER_NAME/users/Admin@$ORG_NAME-$CLUSTER_NAME
   MSP_TLSCACERTS=$ADMIN_PATH/msp/tlscacerts
   MSP_TLSICACERTS=$ADMIN_PATH/msp/tlsintermediatecerts

   ADMIN_TLS=${ADMIN_PATH}/tls
   mkdir -p ${ADMIN_TLS}

   cp -rf $TLSCERT_PATH/cert.pem $ADMIN_TLS/client.crt
   cp -rf $TLSKEY_PATH/*_sk $ADMIN_TLS/client.key
   
   # 获取ca证书
   CATLS_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/tls/cacerts
   ICATLS_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/tls/intermediatecerts
   mkdir -p ${MSP_TLSCACERTS}
   cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
   if [ -d "$ICATLS_PATH" ]; then
      cp -rf $ICATLS_PATH/*.pem $ADMIN_TLS/ca.crt
      mkdir -p ${MSP_TLSICACERTS}
      cp -rf $ICATLS_PATH/*.pem ${MSP_TLSICACERTS}/tlsica.$CLUSTER_NAME-cert.pem
   else
      cp -rf $CATLS_PATH/*.pem $ADMIN_TLS/ca.crt
   fi
}

register_peer() {
   log -n "Start register peer..."
   fabric-ca-client register -H $ROOTPATH/admin --id.secret=123456 --id.name $PEER.$CLUSTER_NAME --id.type peer \
      --id.affiliation "$CLUSTER_NAME" --id.attrs '"role=peer",ecert=true' -u http://$USER_PWD@$CA_URL
   judge_operation "Register peer" $?
   log -n "Start register peer done."
}

enroll_peer() {
   rm -rf $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME/tls
   fabric-ca-client enroll --enrollment.profile tls -u http://$PEER.$CLUSTER_NAME:123456@$CA_URL \
      -H $ROOTPATH/admin --csr.cn=$PEER.$CLUSTER_NAME --csr.hosts=["$PEER.$CLUSTER_NAME"] \
      -M $ROOTPATH/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME/tls
   judge_operation "Enroll peer" $?
   
   PEER_PATH=${CRYPTO_PATH}/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME
   MSP_CATLS=$PEER_PATH/msp/tlscacerts
   mkdir -p $MSP_CATLS
   mkdir -p $PEER_PATH/tls
   TLSCERT_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME/tls/signcerts
   TLSKEY_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME/tls/keystore
   cp -rf $TLSCERT_PATH/cert.pem $PEER_PATH/tls/server.crt
   cp -rf $TLSKEY_PATH/*_sk $PEER_PATH/tls/server.key
   
   # 获取ca证书
   CATLS_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/tls/cacerts
   ICATLS_PATH=${ROOTPATH}/$ORG_NAME-$CLUSTER_NAME/tls/intermediatecerts
   MSP_TLSCACERTS=$PEER_PATH/msp/tlscacerts
   MSP_TLSICACERTS=$PEER_PATH/msp/tlsintermediatecerts
   PEER_TLSCERTSPATH=$ROOTPATH/$ORG_NAME-$CLUSTER_NAME/peers/$PEER.$CLUSTER_NAME/tls
   if [ -d "${PEER_TLSCERTSPATH}/tlsintermediatecerts" ]; then
      cp -rf $ICATLS_PATH/*.pem $PEER_PATH/tls/ca.crt
      mkdir -p ${MSP_TLSCACERTS}
      cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
      mkdir -p ${MSP_TLSICACERTS}
      cp -rf ${ICATLS_PATH}/*.pem $MSP_TLSICACERTS/tlsica.$CLUSTER_NAME-cert.pem
      
   else
      cp -rf $CATLS_PATH/*.pem $PEER_PATH/tls/ca.crt
      mkdir -p ${MSP_TLSCACERTS}
      cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
   fi
}

# start from here
# if [ $OPERATE_TYPE = "peerOrg" ]; then
#    log -n "Generate peer org tls certs..."
#    enroll_ca_admin
#    add_affiliation $DOMAIN_REVERSE
#    affiliation_list
#    get_ca_cert
#    register_admin_user
#    enroll_admin_user
#    log -n "Generate peer org tls certs done."
# elif [ $OPERATE_TYPE = "peer" ]; then
#    log -n "Generate peer tls certs..."
#    register_peer
#    enroll_peer
#    log -n "Generate peer tls certs done."
# else
#    log -n "Operate type error."
#    exit 1
# fi

if [ $OPERATE_TYPE = "add_affiliation" ]; then
   enroll_ca_admin
   add_affiliation $CLUSTER_NAME
elif [ $OPERATE_TYPE = "register_admin_user" ]; then
   enroll_ca_admin
   get_ca_cert
   register_admin_user
elif [ $OPERATE_TYPE = "enroll_admin_user" ]; then
   enroll_admin_user
elif [ $OPERATE_TYPE = "register_peer" ]; then
   register_peer
elif [ $OPERATE_TYPE = "enroll_peer" ]; then
   enroll_peer
else
   log -n "Operate type error."
   exit 1
fi