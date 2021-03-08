#!/bin/bash
#==================主要功能===================
#签发管理员证书
#注册组织
#添加com
#添加联盟.com
#注册联盟管理员（Admin@example.com）
#注册orderer.$CLUSTER_NAME
#签发orderer组织根证书 getcacert
#签发 orderer tls证书
#============================================

# ========= parameter list ===============
# 1. OPERATE_TYPE="ordererOrg" or "orderer"
# 2. CLUSTER_NAME="ksc-blockchain"
# 3. ORDERER="orderer0" if the type is "ordererOrg" this para not use, must input a value
# 4. USER_PWD="admin:adminpw"
# 5. CA_URL="msp.intermediate.ca1:7054"
# 6. /work/share/script/
# 7. /work/share/bin/1.4.5/msp/
# 8. /work/share
# 9. /data/auto

# ========================================
OPERATE_TYPE=$1
CLUSTER_NAME=$2
ORDERER=$3
USER_PWD=$4
CA_URL=$5
FABRIC_OPERATE_SCRIPTS_PATH=$6
FABRIC_CA_CLIENT_TLS_PATH=$7
GENERATE_CERTS_ROOT_PATH=$8
SAVE_CERTS_ROOT_PATH=$9

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

# fabric-ca-client directory
export PATH=$PATH:$FABRIC_CA_CLIENT_TLS_PATH

# running dir is project root dir
# for example:
# 1. Generate orderer org tls certs
#   bash scripts/generate-orderer-tls-certs.sh ordererOrg kledger-auto ksc-blockchain orderer0 example.com com.example com admin1:adminpw tls.intermediate.ca1:7054
# 2. Generate orderer org tls certs
#   bash scripts/generate-orderer-tls-certs.sh orderer kledger-auto ksc-blockchain orderer0 example.com com.example com admin1:adminpw tls.intermediate.ca1:7054
#

# orderer orgs certs root path
ROOTPATH="$GENERATE_CERTS_ROOT_PATH/$CLUSTER_NAME/certs/tls/ordererOrg"
# Path format
SYS_ADMIN_PATH=$ROOTPATH/admin
DOMAIN_TLS_PATH=$ROOTPATH/$CLUSTER_NAME/tls
ADMIN_TLS_PATH=$ROOTPATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls
ORDERER_TLS_PATH=$ROOTPATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls
CRYPTO_PATH="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations"

enroll_ca_admin() {
   log -n "enroll system admin..."
   rm -rf $ROOTPATH/admin
   fabric-ca-client enroll -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   sleep 1
   judge_operation "Enroll system admin" $?
}

remove_affiliation() {
   affiliation=$1
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation remove --force $affiliation
   judge_operation "Remove affiliation" $?
}

add_affiliation() {
   log -n "Add affiliation..."
   affiliation=$1
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation add $affiliation
   judge_operation "Add affiliation" $?
}

affiliation_list() {
   log -n "Affiliation list..."
   fabric-ca-client affiliation list -H ${SYS_ADMIN_PATH}
   judge_operation "Affiliation list" $?
}

get_ca_cert() {
   rm -rf $ROOTPATH/$CLUSTER_NAME/tls
   fabric-ca-client getcacert -M ${DOMAIN_TLS_PATH} -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Get ca cert" $?
   
   MSP_TLSCACERTS=${CRYPTO_PATH}/$CLUSTER_NAME/msp/tlscacerts
   MSP_TLSICACERTS=${CRYPTO_PATH}/$CLUSTER_NAME/msp/tlsintermediatecerts
   CATLS_PATH=${ROOTPATH}/$CLUSTER_NAME/tls/cacerts
   ICATLS_PATH=${ROOTPATH}/$CLUSTER_NAME/tls/intermediatecerts
   
   # root ca
   mkdir -p ${MSP_TLSCACERTS}
   cp -rf ${CATLS_PATH}/*.pem ${MSP_TLSCACERTS}/tlsca.$CLUSTER_NAME-cert.pem
   
   # intermediate ca
   if [ -d ${ICATLS_PATH} ]; then
      mkdir -p ${MSP_TLSICACERTS}
      cp -rf ${ICATLS_PATH}/* $MSP_TLSICACERTS/tlsica.$CLUSTER_NAME-cert.pem
   fi
}

register_admin_user() {
   log -n "Register admin user..."
   fabric-ca-client register --id.name Admin@$CLUSTER_NAME --id.affiliation "$CLUSTER_NAME" \
      --id.attrs '"hf.Registrar.Roles=client,orderer,peer,user","hf.Registrar.DelegateRoles=client,orderer,peer,user",hf.Registrar.Attributes=*,hf.GenCRL=true,hf.Revoker=true,hf.AffiliationMgr=true,hf.IntermediateCA=false,role=admin:ecert' \
      --id.secret=123456 -u http://Admin@$CLUSTER_NAME:123456@$CA_URL -H ${SYS_ADMIN_PATH} 
   judge_operation "Register admin user" $?
}

enroll_admin_user() {
   if [ ! -d $ROOTPATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls ]; then
      rm -rf $ROOTPATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls
      fabric-ca-client enroll -d --enrollment.profile tls -u http://Admin@$CLUSTER_NAME:123456@$CA_URL \
         -M $ROOTPATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls
      judge_operation "Enroll admin user" $?
      
      TLSCERT_PATH=${ROOTPATH}/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/signcerts
      TLSKEY_PATH=${ROOTPATH}/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/tls/keystore

      ADMIN_PATH=${CRYPTO_PATH}/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME
      MSP_TLSCACERTS=$ADMIN_PATH/msp/tlscacerts
      MSP_TLSICACERTS=$ADMIN_PATH/msp/tlsintermediatecerts

      ADMIN_TLS=${ADMIN_PATH}/tls
      mkdir -p ${ADMIN_TLS}

      cp -rf $TLSCERT_PATH/cert.pem $ADMIN_TLS/client.crt
      cp -rf $TLSKEY_PATH/*_sk $ADMIN_TLS/client.key

      CATLS_PATH=${ROOTPATH}/$CLUSTER_NAME/tls/cacerts
      log -n "${CATLS_PATH}"
      ICATLS_PATH=${ROOTPATH}/$CLUSTER_NAME/tls/intermediatecerts
      log -n "${ICATLS_PATH}"
      if [ -d "$ICATLS_PATH" ]; then
         cp -rf $ICATLS_PATH/*.pem $ADMIN_TLS/ca.crt
         mkdir -p ${MSP_TLSCACERTS}
         cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
         mkdir -p ${MSP_TLSICACERTS}
         cp -rf $ICATLS_PATH/*.pem ${MSP_TLSICACERTS}/tlsica.$CLUSTER_NAME-cert.pem
      else
         cp -rf $CATLS_PATH/*.pem $ADMIN_TLS/ca.crt
         mkdir -p ${MSP_TLSCACERTS}
         cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
      fi
   fi
}

register_orderer() {
   log -n "Start register orderer..."
   fabric-ca-client register --id.name  $ORDERER.$CLUSTER_NAME --id.affiliation "$CLUSTER_NAME" --id.secret 123456 \
   --id.type orderer -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Register orderer" $?
}

enroll_orderer() {
   if [ ! -d $ROOTPATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls ]; then
      rm -rf $ROOTPATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls
      fabric-ca-client enroll -u http://$ORDERER.$CLUSTER_NAME:123456@$CA_URL --enrollment.profile tls \
         -H ${SYS_ADMIN_PATH} -M $ROOTPATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls \
         --csr.hosts $ORDERER.$CLUSTER_NAME --csr.cn=$ORDERER.$CLUSTER_NAME
      judge_operation "Enroll orderer" $?
      
      ORDERER_PATH=${CRYPTO_PATH}/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME
      MSP_CATLS=$ORDERER_PATH/msp/tlscacerts
      mkdir -p $MSP_CATLS
      mkdir -p $ORDERER_PATH/tls
      TLSCERT_PATH=${ROOTPATH}/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls/signcerts
      TLSKEY_PATH=${ROOTPATH}/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls/keystore
      cp -rf $TLSCERT_PATH/cert.pem $ORDERER_PATH/tls/server.crt
      cp -rf $TLSKEY_PATH/*_sk $ORDERER_PATH/tls/server.key
      
      CATLS_PATH=${ROOTPATH}/$CLUSTER_NAME/tls/cacerts
      ICATLS_PATH=${ROOTPATH}/$CLUSTER_NAME/tls/intermediatecerts
      MSP_TLSCACERTS=$ORDERER_PATH/msp/tlscacerts
      MSP_TLSICACERTS=$ORDERER_PATH/msp/tlsintermediatecerts
      ODERER_TLSCERTSPATH=$ROOTPATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/tls
      if [ -d "${ODERER_TLSCERTSPATH}/tlsintermediatecerts" ]; then
         cp -rf $ICATLS_PATH/*.pem $ORDERER_PATH/tls/ca.crt
         mkdir -p ${MSP_TLSCACERTS}
         cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
         mkdir -p ${MSP_TLSICACERTS}
         cp -rf ${ICATLS_PATH}/*.pem $MSP_TLSICACERTS/tlsica.$CLUSTER_NAME-cert.pem
      else
         cp -rf $CATLS_PATH/*.pem $ORDERER_PATH/tls/ca.crt
         mkdir -p ${MSP_TLSCACERTS}
         cp -rf $CATLS_PATH/*.pem $MSP_TLSCACERTS/tlsca.$CLUSTER_NAME-cert.pem
      fi
   fi
}

# start from here
if [ $OPERATE_TYPE = "ordererOrg" ]; then
   log -n "Generate orderer org tls certs..."
   enroll_ca_admin
   add_affiliation $CLUSTER_NAME
   affiliation_list
   get_ca_cert
   register_admin_user
   enroll_admin_user
   log -n "Generate orderer org tls certs done."
elif [ $OPERATE_TYPE = "orderer" ]; then
   log -n "Generate orderer tls certs..."
   register_orderer
   enroll_orderer
   log -n "Generate orderer tls certs done."
else
   log -n "Operate type error."
   exit 1
fi
