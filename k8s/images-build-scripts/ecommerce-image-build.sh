#!/bin/sh
export DOCKER_BUILDKIT=1
docker build -f ../../integration-tests/src/main/docker/Dockerfile \
--build-arg MAIN_CLASS_BUILD_TIME_FIXED="it.plansoft.ecommerce.EcommerceApplication" \
--build-arg SPRING_PROFILES_ACTIVE_BUILD_TIME_FIXED=production \
-t ppanconi/ecommerce:0.1.1 ../../ecommerce