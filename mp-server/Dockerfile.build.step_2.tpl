FROM ${GCP_REGION}-docker.pkg.dev/${GCP_PROJECT_ID}/${PROJECT_NAME}-${ENV_NAME}/${APP_NAME}-new:step_1_${SHORT_COMMIT_HASH} as build

WORKDIR /app

RUN mvn -s config/settings.xml clean package -P ${BUILD_PROFILE} -DSkipTests=true

################################################################################

FROM tomcat:9-jdk8

# Clean default webapps
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy your WAR file to ROOT.war (this makes it accessible at `/`)
COPY --chown=nobody:nogroup --from=build /app/web/target/web-mp-casino.war /usr/local/tomcat/webapps/ROOT.war
COPY --chown=nobody:nogroup --chmod=765 --from=build /app/deploy/scripts /opt/scripts

ARG GCS_BUCKET=md-dev01-tools

RUN cd /opt/java && \
    curl -sSL https://dl.google.com/dl/cloudsdk/channels/rapid/downloads/google-cloud-cli-linux-x86_64.tar.gz | tar -C /opt -xz && /opt/google-cloud-sdk/install.sh --quiet --path-update=false --additional-components=gsutil && ln -s /opt/google-cloud-sdk/bin/gsutil /usr/local/bin/gsutil && \
    gsutil cp gs://${GCS_BUCKET}/oracle-jdk8/jre-8u451-linux-x64.tar.gz /opt/java/oracle-jdk8.tar.gz && \
    mkdir -p /opt/java/oracle-jdk8 && tar -xvzf oracle-jdk8.tar.gz -C /opt/java/oracle-jdk8 --strip-components=1

ENV PATH=/usr/local/tomcat/bin:/opt/java/oracle-jdk8/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin

CMD ["bash", "/opt/scripts/gsmp.sh", "start", "mp"]
