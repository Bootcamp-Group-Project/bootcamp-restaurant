FROM nginx:alpine

COPY nginx.conf.template /nginx.conf.template

# envsubst replaces only ${EC2_IP} — nginx vars like $host are left untouched
CMD ["/bin/sh", "-c", \
    "envsubst '${EC2_IP}' < /nginx.conf.template > /etc/nginx/nginx.conf && nginx -g 'daemon off;'"]
