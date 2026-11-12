targetScope = 'resourceGroup'

param resourceSuffix string = '2c297d64'

var tags = { project: 'EcoMind', environment: 'production' }

resource email 'Microsoft.Communication/emailServices@2025-09-01' = {
  name: 'ecs-ecomind-${resourceSuffix}'
  location: 'global'
  tags: tags
  properties: { dataLocation: 'United States' }
}

resource domain 'Microsoft.Communication/emailServices/domains@2025-09-01' = {
  parent: email
  name: 'AzureManagedDomain'
  location: 'global'
  tags: tags
  properties: {
    domainManagement: 'AzureManaged'
    userEngagementTracking: 'Disabled'
  }
}

resource communication 'Microsoft.Communication/communicationServices@2025-09-01' = {
  name: 'acs-ecomind-${resourceSuffix}'
  location: 'global'
  tags: tags
  properties: {
    dataLocation: 'United States'
    linkedDomains: [domain.id]
  }
}

resource vault 'Microsoft.KeyVault/vaults@2024-11-01' existing = {
  name: 'kv-ecomind-${resourceSuffix}'
}

resource connection 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'azure-email-connection-string'
  properties: {
    value: communication.listKeys().primaryConnectionString
    contentType: 'Azure Communication Services connection string'
    attributes: { enabled: true }
  }
}

output senderDomain string = domain.properties.fromSenderDomain
output endpoint string = communication.properties.hostName
