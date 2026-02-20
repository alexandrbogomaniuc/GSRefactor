FROM node:20-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev

COPY src ./src
RUN mkdir -p /data

ENV PORT=18072
ENV STORE_FILE=/data/config-workflow-store.json

EXPOSE 18072
CMD ["node", "src/server.js"]
