#!/bin/sh
set -eu

psql --set=ON_ERROR_STOP=1 <<'SQL'
SELECT 'CREATE ROLE ecomind_app LOGIN'
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'ecomind_app')
\gexec
GRANT CONNECT ON DATABASE ecomind TO ecomind_app;
GRANT USAGE, CREATE ON SCHEMA public TO ecomind_app;
SQL

# psql hashes the password before sending ALTER ROLE, keeping plaintext out of SQL logs.
printf '%s\n%s\n' "$APP_DATABASE_PASSWORD" "$APP_DATABASE_PASSWORD" |
  psql --set=ON_ERROR_STOP=1 --command='\password ecomind_app'

PGUSER=ecomind_app PGPASSWORD="$APP_DATABASE_PASSWORD" psql --set=ON_ERROR_STOP=1 \
  --command='SELECT current_user, current_database(), ssl, version FROM pg_stat_ssl WHERE pid = pg_backend_pid();'
