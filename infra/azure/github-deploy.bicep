targetScope = 'resourceGroup'

param location string = 'westus2'
param identityName string = 'id-ecomind-github-deploy'
param containerAppName string = 'ca-ecomind-api'
param registryName string = 'acrecomind2c297d64'
@description('Exact GitHub OIDC repository segment, including immutable owner/repository IDs.')
param repositorySubject string = 'upc-pre-202620-13980-greenminds@323838917/EcoMind_Backend@1391221057'

resource app 'Microsoft.App/containerApps@2025-07-01' existing = {
  name: containerAppName
}

resource registry 'Microsoft.ContainerRegistry/registries@2025-04-01' existing = {
  name: registryName
}

resource identity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = {
  name: identityName
  location: location
  tags: {
    project: 'EcoMind'
    purpose: 'GitHub develop deployment'
  }
}

resource federation 'Microsoft.ManagedIdentity/userAssignedIdentities/federatedIdentityCredentials@2023-01-31' = {
  parent: identity
  name: 'github-develop'
  properties: {
    issuer: 'https://token.actions.githubusercontent.com'
    subject: 'repo:${repositorySubject}:ref:refs/heads/develop'
    audiences: ['api://AzureADTokenExchange']
  }
}

resource deployRole 'Microsoft.Authorization/roleDefinitions@2022-04-01' = {
  name: guid(resourceGroup().id, 'ecomind-app-image-deployer')
  properties: {
    roleName: 'EcoMind app image deployer'
    description: 'Update the existing API image and read deployment/registry metadata.'
    type: 'CustomRole'
    assignableScopes: [resourceGroup().id]
    permissions: [
      {
        actions: [
          'Microsoft.App/containerApps/read'
          'Microsoft.App/containerApps/write'
          'Microsoft.App/containerApps/revisions/read'
          'Microsoft.ContainerRegistry/registries/read'
        ]
        notActions: []
        dataActions: []
        notDataActions: []
      }
    ]
  }
}

resource appDeployment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(app.id, identity.id, deployRole.id)
  scope: app
  properties: {
    roleDefinitionId: deployRole.id
    principalId: identity.properties.principalId
    principalType: 'ServicePrincipal'
  }
}

resource registryMetadata 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(registry.id, identity.id, deployRole.id)
  scope: registry
  properties: {
    roleDefinitionId: deployRole.id
    principalId: identity.properties.principalId
    principalType: 'ServicePrincipal'
  }
}

var acrPushRoleId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '8311e382-0749-4cb8-b61a-304f252e45ec')

resource registryPush 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(registry.id, identity.id, acrPushRoleId)
  scope: registry
  properties: {
    roleDefinitionId: acrPushRoleId
    principalId: identity.properties.principalId
    principalType: 'ServicePrincipal'
  }
}

output clientId string = identity.properties.clientId
output tenantId string = tenant().tenantId
output subscriptionId string = subscription().subscriptionId
output principalId string = identity.properties.principalId
