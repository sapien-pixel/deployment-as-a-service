#!/bin/bash

export KUBERNETES_PROVIDER=aws
export MASTER_SIZE=t2.micro
export NODE_SIZE=t2.nano
export NUM_NODES=1
export AWS_S3_BUCKET=${RANDOM}-kube
export KUBE_AWS_INSTANCE_PREFIX=$PROJECT_ID
export USER_ID=$USER_ID

docker run -e KUBERNETES_PROVIDER=$KUBERNETES_PROVIDER \
 -e MASTER_SIZE=$MASTER_SIZE \
 -e NODE_SIZE=$NODE_SIZE \
 -e NUM_NODES=$NUM_NODES \
 -e AWS_S3_BUCKET=$AWS_S3_BUCKET \
 -e KUBE_AWS_INSTANCE_PREFIX=$KUBE_AWS_INSTANCE_PREFIX \
 -e USER_ID=$USER_ID
 -i dhruvkalaria/kubernetes-docker-daas \
 /bin/bash -c 'bash kubernetes/cluster/kube-up.sh'