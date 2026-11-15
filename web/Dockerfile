FROM node:24 AS build-stage
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY ./ .
RUN npm run build

FROM openresty/openresty:bullseye AS production-stage
RUN mkdir /app
COPY --from=build-stage /app/dist /app
COPY nginx.conf /usr/local/openresty/nginx/conf/nginx.conf
