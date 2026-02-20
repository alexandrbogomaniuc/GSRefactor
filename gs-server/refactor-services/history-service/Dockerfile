FROM node:20-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev

COPY src ./src
RUN mkdir -p /data

ENV PORT=18077
ENV STORE_FILE=/data/history-service-store.json

EXPOSE 18077
CMD ["node", "src/server.js"]
