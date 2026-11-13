targetScope = 'resourceGroup'

param resourceSuffix string = '2c297d64'

@secure()
param cloudinaryCloudName string
@secure()
param cloudinaryApiKey string
@secure()
param cloudinaryApiSecret string

resource vault 'Microsoft.KeyVault/vaults@2024-11-01' existing = {
  name: 'kv-ecomind-${resourceSuffix}'
}

resource cloudName 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'cloudinary-cloud-name'
  properties: {
    value: cloudinaryCloudName
    contentType: 'Community image storage configuration'
  }
}

resource apiKey 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'cloudinary-api-key'
  properties: {
    value: cloudinaryApiKey
    contentType: 'Community image storage API key'
  }
}

resource apiSecret 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'cloudinary-api-secret'
  properties: {
    value: cloudinaryApiSecret
    contentType: 'Community image storage API secret'
  }
}
