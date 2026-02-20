FROM node:20-alpine

WORKDIR /app
COPY package*.json ./
RUN npm ci --omit=dev || npm install --omit=dev
COPY src ./src

ENV PORT=18079
ENV STORE_FILE=/data/multiplayer-service-store.json
EXPOSE 18079
CMD ["npm", "start"]
