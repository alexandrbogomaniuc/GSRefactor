FROM node:20-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev

COPY src ./src
RUN mkdir -p /data

ENV PORT=18075
ENV STORE_FILE=/data/wallet-adapter-store.json

EXPOSE 18075
CMD ["node", "src/server.js"]
