#!/bin/bash

echo
echo " ____    _____      _      ____    _____ "
echo "/ ___|  |_   _|    / \    |  _ \  |_   _|"
echo "\___ \    | |     / _ \   | |_) |   | |  "
echo " ___) |   | |    / ___ \  |  _ <    | |  "
echo "|____/    |_|   /_/   \_\ |_| \_\   |_|  "
echo
echo "Build your first network (BYFN) end-to-end test"
echo
CHANNEL_NAME="$1"
CLUSTER_NAME=$2
ORDERER_NAME=$3
ORG_NAME=$4
PEER_NAME=$5
#FABRIC_OPERATE_SCRIPTS_PATH=$6
DELAY="$6"
LANGUAGE="$7"
TIMEOUT="$8"
NO_CHAINCODE="${9}"
: ${CHANNEL_NAME:="mychannel"}
: ${DELAY:="3"}
: ${LANGUAGE:="golang"}
: ${TIMEOUT:="10"}
: ${NO_CHAINCODE:="false"}
LANGUAGE=`echo "$LANGUAGE" | tr [:upper:] [:lower:]`

CC_SRC_PATH="github.com/chaincode/chaincode_example02/go/"
if [ "$LANGUAGE" = "node" ]; then
	CC_SRC_PATH="/opt/gopath/src/github.com/chaincode/chaincode_example02/node/"
fi

if [ "$LANGUAGE" = "java" ]; then
	CC_SRC_PATH="/opt/gopath/src/github.com/chaincode/chaincode_example02/java/"
fi

echo "Channel name : "$CHANNEL_NAME

set -e
#. /opt/gopath/src/github.com/hyperledger/fabric/peer/scripts/env.sh

ORG_LOWER_CASE=$(echo ${ORG_NAME} | tr '[A-Z]' '[a-z]')

# verify the result of the end-to-end test
verifyResult() {
  if [ $1 -ne 0 ]; then
    echo "!!!!!!!!!!!!!!! "$2" !!!!!!!!!!!!!!!!"
    echo "========= ERROR !!! FAILED to execute End-2-End Scenario ==========="
    echo
    exit 1
  fi
}

setGlobals() {
  export CORE_PEER_TLS_ENABLED=true
  export CORE_PEER_LOCALMSPID=${ORG_NAME}MSP
  export CORE_PEER_ADDRESS=$PEER_NAME.$CLUSTER_NAME:7051
  export CORE_PEER_TLS_CERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/peers/$PEER_NAME.$CLUSTER_NAME/tls/server.crt
  export CORE_PEER_TLS_KEY_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/peers/$PEER_NAME.$CLUSTER_NAME/tls/server.key
  export CORE_PEER_TLS_ROOTCERT_FILE=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/peers/$PEER_NAME.$CLUSTER_NAME/tls/ca.crt
  export CORE_PEER_MSPCONFIGPATH=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/peerOrganizations/$ORG_LOWER_CASE-$CLUSTER_NAME/users/Admin@$ORG_LOWER_CASE-$CLUSTER_NAME/msp
  export ORDERER_CA=/opt/gopath/src/github.com/hyperledger/fabric/peer/crypto/ordererOrganizations/$CLUSTER_NAME/orderers/$ORDERER_NAME.$CLUSTER_NAME/msp/tlscacerts/tlsca.$CLUSTER_NAME-cert.pem
}

installChaincode() {
  VERSION=${1:-1.0}
  set -x
  peer chaincode install -n mycc -v ${VERSION} -l ${LANGUAGE} -p ${CC_SRC_PATH} >&log.txt
  res=$?
  set +x
  cat log.txt
  verifyResult $res "Chaincode installation on $PEER_NAME.$ORG_LOWER_CASE has failed"
  echo "===================== Chaincode is installed on $PEER_NAME.$ORG_LOWER_CASE ===================== "
  echo
}

instantiateChaincode() {
  VERSION=${1:-1.0}

  # while 'peer chaincode' command can get the orderer endpoint from the peer
  # (if join was successful), let's supply it directly as we know it using
  # the "-o" option
  if [ -z "$CORE_PEER_TLS_ENABLED" -o "$CORE_PEER_TLS_ENABLED" = "false" ]; then
    set -x
    peer chaincode instantiate -o $ORDERER_NAME.$CLUSTER_NAME:7050 -C $CHANNEL_NAME -n mycc -l ${LANGUAGE} -v ${VERSION} -c '{"Args":["init","a","100","b","200"]}' -P "OR ('${ORG_NAME}MSP.peer')" >&log.txt
    res=$?
    set +x
  else
    set -x
    peer chaincode instantiate -o $ORDERER_NAME.$CLUSTER_NAME:7050 --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA -C $CHANNEL_NAME -n mycc -l ${LANGUAGE} -v ${VERSION} -c '{"Args":["init","a","100","b","200"]}' -P "OR ('${ORG_NAME}MSP.peer')" >&log.txt
    res=$?
    set +x
  fi
  cat log.txt
  verifyResult $res "Chaincode instantiation on $PEER_NAME.$ORG_LOWER_CASE on channel '$CHANNEL_NAME' failed"
  echo "===================== Chaincode is instantiated on $PEER_NAME.$ORG_LOWER_CASE on channel '$CHANNEL_NAME' ===================== "
  echo
}

chaincodeInvoke() {
  # while 'peer chaincode' command can get the orderer endpoint from the
  # peer (if join was successful), let's supply it directly as we know
  # it using the "-o" option
  if [ -z "$CORE_PEER_TLS_ENABLED" -o "$CORE_PEER_TLS_ENABLED" = "false" ]; then
    set -x
    peer chaincode invoke -o $ORDERER_NAME.$CLUSTER_NAME:7050 -C $CHANNEL_NAME -n mycc -c '{"Args":["invoke","a","b","10"]}' >&log.txt
    res=$?
    set +x
  else
    set -x
    peer chaincode invoke -o $ORDERER_NAME.$CLUSTER_NAME:7050 --tls $CORE_PEER_TLS_ENABLED --cafile $ORDERER_CA -C $CHANNEL_NAME -n mycc -c '{"Args":["invoke","a","b","10"]}' >&log.txt
    res=$?
    set +x
  fi
  cat log.txt
  verifyResult $res "Invoke execution on $PEER_NAME.$ORG_LOWER_CASE failed "
  echo "===================== Invoke transaction successful on $PEER_NAME.$ORG_LOWER_CASE on channel '$CHANNEL_NAME' ===================== "
  echo
}

chaincodeQuery() {
  EXPECTED_RESULT=$1
  echo "===================== Querying on $PEER_NAME.$ORG_LOWER_CASE on channel '$CHANNEL_NAME'... ===================== "
  local rc=1
  local starttime=$(date +%s)

  # continue to poll
  # we either get a successful response, or reach TIMEOUT
  while
    test "$(($(date +%s) - starttime))" -lt "$TIMEOUT" -a $rc -ne 0
  do
    sleep $DELAY
    echo "Attempting to Query $PEER_NAME.$ORG_LOWER_CASE ...$(($(date +%s) - starttime)) secs"
    set -x
    peer chaincode query -C $CHANNEL_NAME -n mycc -c '{"Args":["query","a"]}' >&log.txt
    res=$?
    set +x
    test $res -eq 0 && VALUE=$(cat log.txt | awk '/Query Result/ {print $NF}')
    test "$VALUE" = "$EXPECTED_RESULT" && rc=0
    # removed the string "Query Result" from peer chaincode query command
    # result. as a result, have to support both options until the change
    # is merged.
    test $rc -ne 0 && VALUE=$(cat log.txt | egrep '^[0-9]+$')
    test "$VALUE" = "$EXPECTED_RESULT" && rc=0
  done
  echo
  cat log.txt
  if test $rc -eq 0; then
    echo "===================== Query successful on $PEER_NAME.$ORG_LOWER_CASE on channel '$CHANNEL_NAME' ===================== "
  else
    echo "!!!!!!!!!!!!!!! Query result on $PEER_NAME.$ORG_LOWER_CASE is INVALID !!!!!!!!!!!!!!!!"
    echo "================== ERROR !!! FAILED to execute End-2-End Scenario =================="
    echo
    exit 1
  fi
}

if [ "${NO_CHAINCODE}" != "true" ]; then
    setGlobals

    echo "Install chaincode"
    installChaincode

    echo "Instantiating chaincode on $PEER_NAME.$ORG_LOWER_CASE..."
    instantiateChaincode

	echo "Querying chaincode on $PEER_NAME.$ORG_LOWER_CASE..."
	chaincodeQuery 100

    echo "Sending invoke transaction on $PEER_NAME.$ORG_LOWER_CASE..."
    chaincodeInvoke

    echo "Querying chaincode on $PEER_NAME.$ORG_LOWER_CASE..."
    chaincodeQuery 90
fi

echo
echo "========= All GOOD, BYFN execution completed =========== "
echo

echo
echo " _____   _   _   ____   "
echo "| ____| | \ | | |  _ \  "
echo "|  _|   |  \| | | | | | "
echo "| |___  | |\  | | |_| | "
echo "|_____| |_| \_| |____/  "
echo

exit 0