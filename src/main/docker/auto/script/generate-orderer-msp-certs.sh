#!/bin/bash
#==================主要功能===================
#签发管理员证书
#注册组织
#添加com
#添加联盟.com
#注册联盟管理员（Admin@example.com）
#注册orderer.$DOMAIN
#签发orderer组织根证书 getcert
#签发 orderer msp证书
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
FABRIC_CA_CLIENT_MSP_PATH=$7
GENERATE_CERTS_ROOT_PATH=$8
SAVE_CERTS_ROOT_PATH=$9

set -e
source $FABRIC_OPERATE_SCRIPTS_PATH/env.sh

# fabric-ca-client directory
export PATH=$PATH:$FABRIC_CA_CLIENT_MSP_PATH

# running dir is project root dir
# for example:
# 1. Generate orderer org msp certs
#   bash scripts/generate-orderer-msp-certs.sh ordererOrg kledger-auto ksc-blockchain orderer0 example.com com.example com admin1:adminpw msp.intermediate.ca1:7054
# 2. Generate orderer msp certs
#   bash scripts/generate-orderer-msp-certs.sh orderer kledger-auto ksc-blockchain orderer0 example.com com.example com admin1:adminpw msp.intermediate.ca1:7054
#

# orderer orgs certs root path
ROOTPATH="$GENERATE_CERTS_ROOT_PATH/$CLUSTER_NAME/certs/msp/ordererOrg"

# Path format
SYS_ADMIN_PATH=$ROOTPATH/admin
DOMAIN_MSP_PATH=$ROOTPATH/$CLUSTER_NAME/msp
ADMIN_MSP_PATH=$ROOTPATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME/msp
ORDERER_MSP_PATH=$ROOTPATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME/msp

CRYPTO_PATH="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations"

enroll_ca_admin() {
   log -n "Enroll system admin ..."
   rm -rf $ROOTPATH/admin
   echo fabric-ca-client enroll -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   fabric-ca-client enroll -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Enroll system admin" $?
}

remove_affiliation() {
   log -n "Remove affiliation..."
   affiliation=$1
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation remove --force $affiliation
   judge_operation "Remove affiliation" $?
   log -n "Remove affiliation done."
}

add_affiliation() {
   log -n "Add affiliation..."
   echo fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation add $1
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation add $1
   judge_operation "Remove affiliation" $?
   log -n "Add affiliation done."
}

# affiliation list
affiliation_list() {
   log -n "Affiliation list..."
   fabric-ca-client -H ${SYS_ADMIN_PATH} affiliation list
   judge_operation "Affiliation list" $?
   log -n "Affiliation list done."
}

get_ca_cert() {
   log -n "Get ca cert..."
   fabric-ca-client getcacert -M ${DOMAIN_MSP_PATH} -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Get ca cert" $?
   mkdir $DOMAIN_MSP_PATH/admincerts
   rm -rf $DOMAIN_MSP_PATH/keystore
   rm -rf $DOMAIN_MSP_PATH/signcerts
   rm -rf $DOMAIN_MSP_PATH/user
   rm -rf $DOMAIN_MSP_PATH/Issuer*
   mv $DOMAIN_MSP_PATH/cacerts/*.pem $DOMAIN_MSP_PATH/cacerts/ca.$CLUSTER_NAME-cert.pem
   if [ -d "$DOMAIN_MSP_PATH/intermediatecerts" ]; then
      rm -rf ${DOMAIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      mv ${DOMAIN_MSP_PATH}/intermediatecerts/*.pem ${DOMAIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
   fi

   #copy cert to data
  mkdir -p $CRYPTO_PATH/$CLUSTER_NAME/msp/
  cp -rf $DOMAIN_MSP_PATH/ $CRYPTO_PATH/$CLUSTER_NAME/

   log -n "Get ca cert done."
}

register_admin_user() {
   log -n "Register admin user..."
   fabric-ca-client register --id.name Admin@$CLUSTER_NAME --id.affiliation "$CLUSTER_NAME" --id.secret=123456 \
      --id.attrs 'admin=true:ecert' \
      -u http://$USER_PWD@$CA_URL -H ${SYS_ADMIN_PATH}
   judge_operation "Register admin user" $?
   log -n "Register admin user done."
}

enroll_admin_user() {
   log -n "Enroll admin user..."
   cp $FABRIC_CA_CLIENT_MSP_PATH/fabric-ca-client-config.yaml ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/caurl/$CA_URL/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/cnname/Admin@$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/ouname/client/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/domainstr/$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   if [ ! -d $ADMIN_MSP_PATH ]; then   
      fabric-ca-client enroll  -u http://Admin@$CLUSTER_NAME:123456@$CA_URL -H ${SYS_ADMIN_PATH} -M $ADMIN_MSP_PATH
      judge_operation "Enroll admin user" $?
      mkdir $ADMIN_MSP_PATH/admincerts
      rm -rf $ADMIN_MSP_PATH/user
      rm -rf $ADMIN_MSP_PATH/Issuer*
      cp -rf $ADMIN_MSP_PATH/signcerts/cert.pem $DOMAIN_MSP_PATH/admincerts/Admin@$CLUSTER_NAME-cert.pem
      cp -rf $ADMIN_MSP_PATH/signcerts/cert.pem $CRYPTO_PATH/$CLUSTER_NAME/msp/admincerts

      cp -rf $ADMIN_MSP_PATH/signcerts/cert.pem $ADMIN_MSP_PATH/admincerts/Admin@$CLUSTER_NAME-cert.pem
      rm -rf $ADMIN_MSP_PATH/signcerts/Admin@$CLUSTER_NAME-cert.pem
      mv $ADMIN_MSP_PATH/signcerts/cert.pem $ADMIN_MSP_PATH/signcerts/Admin@$CLUSTER_NAME-cert.pem
      rm -rf $ADMIN_MSP_PATH/cacerts/ca.$CLUSTER_NAME-cert.pem
      mv $ADMIN_MSP_PATH/cacerts/*.pem $ADMIN_MSP_PATH/cacerts/ca.$CLUSTER_NAME-cert.pem
      if [ -d "$ADMIN_MSP_PATH/intermediatecerts" ]; then
         rm -rf ${ADMIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
         mv ${ADMIN_MSP_PATH}/intermediatecerts/*.pem ${ADMIN_MSP_PATH}/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      fi

      #copy cert to data
      mkdir -p $CRYPTO_PATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME
      cp -rf $ADMIN_MSP_PATH $CRYPTO_PATH/$CLUSTER_NAME/users/Admin@$CLUSTER_NAME

   fi
   log -n "Enroll admin user done."
}

register_orderer() {
   log -n "Register orderer..."
   fabric-ca-client register -H ${SYS_ADMIN_PATH} --id.secret=123456 --id.name $ORDERER.$CLUSTER_NAME --id.type orderer \
      --id.affiliation "$CLUSTER_NAME" --id.maxenrollments "0" -u http://$USER_PWD:123456@$CA_URL
   judge_operation "Register orderer" $?
   log -n "Register orderer done."
}

enroll_orderer() {
   log -n "Enroll orderer..."
   cp -rf  $FABRIC_CA_CLIENT_MSP_PATH/fabric-ca-client-config.yaml ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sleep 1
   sed -i "s/caurl/$CA_URL/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/cnname/$ORDERER.$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/ouname/orderer/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml
   sed -i "s/domainstr/$CLUSTER_NAME/g" ${SYS_ADMIN_PATH}/fabric-ca-client-config.yaml

   if [ ! -d $ORDERER_MSP_PATH ]; then
      fabric-ca-client enroll -u http://$ORDERER.$CLUSTER_NAME:123456@$CA_URL -H ${SYS_ADMIN_PATH} -M $ORDERER_MSP_PATH
      judge_operation "Enroll orderer" $?
      mkdir -p $ORDERER_MSP_PATH/admincerts
      cp -rf $ADMIN_MSP_PATH/signcerts/Admin@$CLUSTER_NAME-cert.pem $ORDERER_MSP_PATH/admincerts/
      rm -rf $ORDERER_MSP_PATH/signcerts/$ORDERER.$CLUSTER_NAME-cert.pem
      mv $ORDERER_MSP_PATH/signcerts/*.pem $ORDERER_MSP_PATH/signcerts/$ORDERER.$CLUSTER_NAME-cert.pem
      rm -rf $ORDERER_MSP_PATH/user
      rm -rf $ORDERER_MSP_PATH/Issuer*
      mv $ORDERER_MSP_PATH/cacerts/*.pem $ORDERER_MSP_PATH/cacerts/ca.$CLUSTER_NAME-cert.pem
      if [ -d $ORDERER_MSP_PATH/intermediatecerts ]; then
         rm -rf $ORDERER_MSP_PATH/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
         mv $ORDERER_MSP_PATH/intermediatecerts/*.pem $ORDERER_MSP_PATH/intermediatecerts/ca.$CLUSTER_NAME-cert.pem
      fi

      #copy cert to data
      mkdir -p $CRYPTO_PATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME
      cp -rf $ORDERER_MSP_PATH $CRYPTO_PATH/$CLUSTER_NAME/orderers/$ORDERER.$CLUSTER_NAME
   fi
   log -n "Enroll orderer done."
}

copy_certs_to_crypto_config_dir() {
   log -n "Copy certs ..."
   CRYPTO_PATH="$SAVE_CERTS_ROOT_PATH/$CLUSTER_NAME/crypto-config/ordererOrganizations"
   if [ ! -d $CRYPTO_PATH ]; then
      mkdir -p ${CRYPTO_PATH}
   else
      rm -rf $CRYPTO_PATH/*
   fi
   cp -rf $ROOTPATH/$CLUSTER_NAME/ $CRYPTO_PATH
   log -n "Copy certs done."
}

### start from here
if [ $OPERATE_TYPE = "ordererOrg" ]; then
   enroll_ca_admin
   add_affiliation $CLUSTER_NAME
   affiliation_list
   get_ca_cert
   register_admin_user
   enroll_admin_user
#   copy_certs_to_crypto_config_dir
elif [ $OPERATE_TYPE = "orderer" ]; then
   register_orderer
   enroll_orderer
#   copy_certs_to_crypto_config_dir
else
   log -n "Operate type error."
   exit 1
fi