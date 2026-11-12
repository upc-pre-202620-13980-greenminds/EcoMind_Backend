targetScope = 'resourceGroup'

@secure()
@description('Private, additive SQL for reserved demonstration accounts. Never commit a live parameter file.')
param seedSql string
param resourceSuffix string = '2c297d64'
param location string = 'westus2'

resource vault 'Microsoft.KeyVault/vaults@2024-11-01' existing = {
  name: 'kv-ecomind-${resourceSuffix}'
}

module seed './demo-data-job.bicep' = {
  name: 'ecomind-demo-data-job'
  params: {
    location: location
    resourceSuffix: resourceSuffix
    seedSql: seedSql
    databasePassword: vault.getSecret('db-app-password')
  }
}
