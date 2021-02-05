#!/bin/bash
set -e
# 0: false 1: true
# TLS_ENABLED="true"
TLS_ENABLED=${KLEDGER_TLS_ENABLE}

# print log function 
function log {
   if [ "$1" = "-n" ]; then
      shift
      echo -n -e "\033[35m ##### `date '+%Y-%m-%d %H:%M:%S'` $* \033[0m"
      echo ""
   else
      echo -e "\033[35m ##### `date '+%Y-%m-%d %H:%M:%S'` $* \033[35m"
      echo ""
   fi
}

# judge operation if success
function judge_operation {
   operate_des=$1
   operate_code=$2
   if [ $operate_code = 0 ]; then
      log -n "${operate_des} success"
   else 
      log -n "${operate_des} failed."
      exit 1
   fi
}

# judge str1 contain str2
function judge_str_contain {
   result=$(echo "$1" | grep "$2")
   if [ "$result" = "" ]; then
      echo "false"
   else
      echo "true"
   fi
}