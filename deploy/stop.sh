#!/bin/bash
kubectl delete -f auto-deployment.yaml
docker rmi bc/auto:1.0-SNAPSHOT
kubectl delete ns mycluster
kubectl delete pv mycluster-pv
rm -rf /home/nfs_data/*
