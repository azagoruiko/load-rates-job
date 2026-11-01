#!/usr/bin/env bash
VER=$1
BUILD=$2
if [[ ! -z $BUILD ]]; then
    mvn clean install
fi
docker build "--tag=docker-registry.service.consul:5001/docker/rates-update-job:${VER}" ./
docker push "docker-registry.service.consul:5001/docker/rates-update-job:${VER}"
export NOMAD_ADDR=http://10.7.0.1:4646
/usr/bin/nomad job run update-rates-job.nomad
