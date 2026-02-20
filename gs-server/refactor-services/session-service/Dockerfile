FROM node:20-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev

COPY src ./src
RUN mkdir -p /data

ENV PORT=18073
ENV STORE_FILE=/data/session-service-store.json

EXPOSE 18073
CMD ["node", "src/server.js"]
