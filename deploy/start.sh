#!/bin/bash

cd ..
mvn clean package docker:build -Dmaven.test.skip=true
cd deploy/
kubectl create -f auto-deployment.yaml
