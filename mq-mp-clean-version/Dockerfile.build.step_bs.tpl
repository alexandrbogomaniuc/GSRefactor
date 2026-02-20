FROM ${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${PROJECT_NAME}-${ENV_NAME}/${APP_NAME}-new:step_1_${SHORT_COMMIT_HASH} as build

WORKDIR /app

RUN mvn -s config/settings.xml clean package -P ${BUILD_PROFILE} -DSkipTests=true

################################################################################

FROM maven:3.9-eclipse-temurin-8

RUN mkdir -p /app

WORKDIR /app

ARG GCS_BUCKET=md-dev01-tools
ARG BOTS_SH_SRC_FILE=bots_development_maxduel.sh

COPY --chown=nobody:nogroup --from=build /app/bots/target/mp-bots-1.0.0-SNAPSHOT.jar /app/mp-bots-1.0.0-SNAPSHOT.jar
COPY --chown=nobody:nogroup --from=build /app/bots/target/lib /app/lib
COPY --chown=nobody:nogroup --from=build /app/bots/scripts/${BOTS_SH_SRC_FILE} /app/bots.sh

RUN cd /opt/java && \
    curl -sSL https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-linux-x86_64.tar.gz | tar -C /opt -xz && /opt/google-cloud-sdk/install.sh --quiet --path-update=false --additional-components=gsutil && ln -s /opt/google-cloud-sdk/bin/gsutil /usr/local/bin/gsutil && \
    gsutil cp gs://${GCS_BUCKET}/oracle-jdk8/jre-8u451-linux-x64.tar.gz /opt/java/oracle-jdk8.tar.gz && \
    mkdir -p /opt/java/oracle-jdk8 && tar -xvzf oracle-jdk8.tar.gz -C /opt/java/oracle-jdk8 --strip-components=1

ENV PATH=/usr/local/tomcat/bin:/opt/java/oracle-jdk8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

CMD ["bash", "/app/bots.sh"]
