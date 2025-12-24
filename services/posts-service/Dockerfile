FROM node:18-alpine

WORKDIR /app

# Copy root package files for workspace resolution
COPY package*.json ./
COPY tsconfig.json ./

# Copy shared package
COPY shared/ ./shared/

# Copy service files
COPY services/posts-service/package*.json ./services/posts-service/
COPY services/posts-service/tsconfig.json ./services/posts-service/
COPY services/posts-service/src ./services/posts-service/src

# Install dependencies
WORKDIR /app/shared
RUN npm install && npm run build

WORKDIR /app/services/posts-service
RUN npm install && npm run build

EXPOSE 4404

CMD ["npm", "start"]
