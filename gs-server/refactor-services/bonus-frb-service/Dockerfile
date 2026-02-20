FROM node:20-alpine

WORKDIR /app
COPY package.json ./
RUN npm install --omit=dev

COPY src ./src
RUN mkdir -p /data

ENV PORT=18076
ENV STORE_FILE=/data/bonus-frb-service-store.json

EXPOSE 18076
CMD ["node", "src/server.js"]
