FROM maven:3.9-eclipse-temurin-8-alpine AS build

WORKDIR /app

COPY --chown=nobody:nogroup . .

ARG BUILD_PROFILE
ARG GCS_BUCKET=md-dev01-tools

RUN apk add --update --no-cache python3 && ln -sf python3 /usr/bin/python


RUN cd /opt/java && \
    curl -sSL https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-linux-x86_64.tar.gz | tar -C /opt -xz && /opt/google-cloud-sdk/install.sh --quiet --path-update=false --additional-components=gsutil && ln -s /opt/google-cloud-sdk/bin/gsutil /usr/local/bin/gsutil && \
    gsutil cp gs://${GCS_BUCKET}/oracle-jdk8/jre-8u451-linux-x64.tar.gz /opt/java/oracle-jdk8.tar.gz && \
    tar -xvzf oracle-jdk8.tar.gz && \
    export PATH=:/opt/java/oracle-jdk8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

RUN cd mq-gs && \
    ./build-all-modules.sh && \
    cd cassandra-cache && \
    mvn install -Dorg.slf4j.simpleLogger.defaultLogLevel=error && \
    cd .. && \
    cd .. && \
    mvn -s config/settings.xml test -P ${BUILD_PROFILE}
