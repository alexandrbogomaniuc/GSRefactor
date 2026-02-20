FROM docker:27-cli

RUN apk add --no-cache nodejs npm bash

WORKDIR /app

COPY package.json /app/package.json
COPY src /app/src
COPY scripts /app/scripts
COPY public /app/public
COPY README.md /app/README.md

RUN chmod +x /app/scripts/*.sh
RUN mkdir -p /app/data

ENV CM_PORT=7070
ENV CM_CASSANDRA_CONTAINER=gp3-c1-1
ENV CM_CORE_FILE=/app/data/cm-core.json
ENV CM_MIRROR_FILE=/app/data/cm-mirror.json

EXPOSE 7070

ENTRYPOINT ["node", "src/server.js"]
