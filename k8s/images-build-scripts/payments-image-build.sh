#!/bin/sh
export DOCKER_BUILDKIT=1
docker build -f ../../integration-tests/src/main/docker/Dockerfile \
--build-arg MAIN_CLASS_BUILD_TIME_FIXED="it.plansoft.payments.PaymentsApplication" \
--build-arg SPRING_PROFILES_ACTIVE_BUILD_TIME_FIXED=production \
-t ppanconi/payments:0.1.1 ../../payments