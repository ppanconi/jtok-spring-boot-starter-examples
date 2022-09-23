# syntax=docker/dockerfile:experimental
FROM openjdk:11-jdk-slim-buster AS build
WORKDIR /workspace/app

COPY gradlew settings.gradle ./
COPY gradle gradle
COPY build.gradle .
COPY src src

RUN --mount=type=cache,target=/root/.gradle ./gradlew clean build -x test
RUN rm build/libs/*-plain.jar
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)

FROM openjdk:11-jre-slim-buster
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/build/dependency
ARG MAIN_CLASS_BUILD_TIME_FIXED
ARG SPRING_PROFILES_ACTIVE_BUILD_TIME_FIXED=integration
ENV MAIN_CLASS=${MAIN_CLASS_BUILD_TIME_FIXED}
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE_BUILD_TIME_FIXED}
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["sh", "-c", "exec java -cp app:app/lib/* ${MAIN_CLASS} ${0} ${@}"]