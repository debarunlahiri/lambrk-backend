FROM node:18-alpine

WORKDIR /app

# Copy root package files for workspace resolution
COPY package*.json ./
COPY tsconfig.json ./

# Copy shared package
COPY shared/ ./shared/

# Copy service files
COPY services/interaction-service/package*.json ./services/interaction-service/
COPY services/interaction-service/tsconfig.json ./services/interaction-service/
COPY services/interaction-service/src ./services/interaction-service/src

# Install dependencies
WORKDIR /app/shared
RUN npm install && npm run build

WORKDIR /app/services/interaction-service
RUN npm install && npm run build

EXPOSE 3105

CMD ["npm", "start"]
