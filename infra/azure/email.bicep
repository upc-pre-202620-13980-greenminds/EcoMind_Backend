targetScope = 'resourceGroup'

param resourceSuffix string = '2c297d64'
@allowed(['Europe', 'United States'])
param emailDataLocation string = 'Europe'

@description('Enable after this endpoint delivers a test, together with the matching application sender rollout.')
param configureConnectionSecret bool = false

var emailResourceSuffix = emailDataLocation == 'Europe' ? '${resourceSuffix}-eu' : resourceSuffix
var tags = { project: 'EcoMind', environment: 'production' }

resource email 'Microsoft.Communication/emailServices@2025-09-01' = {
  name: 'ecs-ecomind-${emailResourceSuffix}'
  location: 'global'
  tags: tags
  properties: { dataLocation: emailDataLocation }
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

resource defaultSender 'Microsoft.Communication/emailServices/domains/senderUsernames@2025-09-01' = {
  parent: domain
  name: 'donotreply'
  properties: {
    username: 'donotreply'
    displayName: 'EcoMind'
  }
}

resource communication 'Microsoft.Communication/communicationServices@2025-09-01' = {
  name: 'acs-ecomind-${emailResourceSuffix}'
  location: 'global'
  tags: tags
  properties: {
    dataLocation: emailDataLocation
    linkedDomains: [domain.id]
  }
}

resource vault 'Microsoft.KeyVault/vaults@2024-11-01' existing = {
  name: 'kv-ecomind-${resourceSuffix}'
}

resource connection 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = if (configureConnectionSecret) {
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
