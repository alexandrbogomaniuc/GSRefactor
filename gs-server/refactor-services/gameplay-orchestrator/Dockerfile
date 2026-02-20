FROM node:20-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev

COPY src ./src
RUN mkdir -p /data

ENV PORT=18074
ENV STORE_FILE=/data/gameplay-orchestrator-store.json

EXPOSE 18074
CMD ["node", "src/server.js"]
