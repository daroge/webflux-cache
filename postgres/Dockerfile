FROM postgres:11.5-alpine
COPY schema.sql /docker-entrypoint-initdb.d/schema.sql
ENV PGDATA=/var/lib/postgresql/data
EXPOSE 5432