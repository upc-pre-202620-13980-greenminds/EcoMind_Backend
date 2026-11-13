targetScope = 'resourceGroup'

param location string
param resourceSuffix string
@secure()
param databasePassword string
@secure()
param seedSql string

resource environment 'Microsoft.App/managedEnvironments@2025-07-01' existing = {
  name: 'cae-ecomind-prod-${location}'
}

resource database 'Microsoft.DBforPostgreSQL/flexibleServers@2025-08-01' existing = {
  name: 'psql-ecomind-${resourceSuffix}-${location}'
}

resource job 'Microsoft.App/jobs@2025-07-01' = {
  name: 'job-ecomind-demo-data'
  location: location
  tags: { project: 'EcoMind', environment: 'production', purpose: 'demonstration-data' }
  properties: {
    environmentId: environment.id
    workloadProfileName: 'Consumption'
    configuration: {
      triggerType: 'Manual'
      replicaTimeout: 180
      replicaRetryLimit: 0
      manualTriggerConfig: { parallelism: 1, replicaCompletionCount: 1 }
      secrets: [
        { name: 'db-password', value: databasePassword }
        { name: 'seed-sql', value: seedSql }
      ]
    }
    template: {
      containers: [
        {
          name: 'seed'
          image: 'postgres:17-alpine@sha256:e5d2edd28b1f8d40a0a6c0c9cdc545b19b56a0d5d88f94bc11a97d970d4700d0'
          command: ['/bin/sh', '-c']
          args: ['set -eu; printf "%s" "$SEED_SQL" | psql -X --set=ON_ERROR_STOP=1 --set=VERBOSITY=terse --set=SHOW_CONTEXT=never --quiet']
          resources: { cpu: json('0.25'), memory: '0.5Gi' }
          env: [
            { name: 'PGHOST', value: database.properties.fullyQualifiedDomainName }
            { name: 'PGPORT', value: '5432' }
            { name: 'PGDATABASE', value: 'ecomind' }
            { name: 'PGUSER', value: 'ecomind_app' }
            { name: 'PGPASSWORD', secretRef: 'db-password' }
            { name: 'PGSSLMODE', value: 'verify-full' }
            { name: 'PGSSLROOTCERT', value: '/etc/ssl/certs/ca-certificates.crt' }
            { name: 'SEED_SQL', secretRef: 'seed-sql' }
          ]
        }
      ]
    }
  }
}
