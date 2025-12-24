FROM node:18-alpine

WORKDIR /app

# Copy root package files for workspace resolution
COPY package*.json ./
COPY tsconfig.json ./

# Copy shared package
COPY shared/ ./shared/

# Copy service files
COPY services/bitz-service/package*.json ./services/bitz-service/
COPY services/bitz-service/tsconfig.json ./services/bitz-service/
COPY services/bitz-service/src ./services/bitz-service/src

# Install dependencies
WORKDIR /app/shared
RUN npm install && npm run build

WORKDIR /app/services/bitz-service
RUN npm install && npm run build

EXPOSE 4403

CMD ["npm", "start"]
