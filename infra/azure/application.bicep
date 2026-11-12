targetScope = 'resourceGroup'

@allowed(['westus2', 'centralus'])
param location string = 'westus2'
param resourceSuffix string = '2c297d64'
param registryName string = 'acrecomind2c297d64'

@description('Use the verified immutable image digest from the existing private registry.')
@minLength(71)
@maxLength(71)
param imageDigest string

@description('Enable only after storing a real Resend key as resend-api-key in the project vault.')
param configureEmail bool = false
param resendFromEmail string = ''

resource vault 'Microsoft.KeyVault/vaults@2024-11-01' existing = {
  name: 'kv-ecomind-${resourceSuffix}'
}

// Nested secure parameters keep secret values out of the portal form and source files.
module runtime './application-resources.bicep' = {
  name: 'ecomind-api-runtime'
  params: {
    location: location
    resourceSuffix: resourceSuffix
    registryName: registryName
    imageDigest: imageDigest
    applicationDatabasePassword: vault.getSecret('db-app-password')
    jwtSigningKey: vault.getSecret('jwt-signing-key')
    resendApiKey: configureEmail ? vault.getSecret('resend-api-key') : ''
    resendFromEmail: resendFromEmail
  }
}

output apiUrl string = runtime.outputs.apiUrl
