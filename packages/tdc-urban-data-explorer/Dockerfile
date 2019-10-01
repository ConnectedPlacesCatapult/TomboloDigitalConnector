# Mapnik requires GLIBCXX_3.4.21, which is not available on standard Jessie deb build for Docker node image
# so build from scratch

FROM mhart/alpine-node:8
WORKDIR /app
COPY package.json yarn.lock ./
RUN yarn install --production

# Only copy over the node pieces we need from the above image
FROM alpine:3.6

RUN echo '@testing http://dl-cdn.alpinelinux.org/alpine/edge/testing' >> /etc/apk/repositories
RUN apk --no-cache --update add libc6-compat tini gdal@testing proj4@testing proj4-dev@testing

COPY --from=0 /usr/bin/node /usr/bin/
COPY --from=0 /usr/lib/libgcc* /usr/lib/libstdc* /usr/lib/
WORKDIR /app
COPY --from=0 /app .
COPY . .

EXPOSE 3000

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["node", "--max_old_space_size=300", "./build/www.js"]
