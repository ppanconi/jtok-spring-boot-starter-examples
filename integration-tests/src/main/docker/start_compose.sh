#!/bin/sh
export DOCKER_BUILDKIT=1
export ECOMMERCE_PASSWORD=ecommerce
export PAYMENTS_PASSWORD=payments
export DEPOT_PASSWORD=depot
export POSTGRES_PASSWORD=postgres
docker-compose up -d zookeeper kafka kafdrop postgres mongo