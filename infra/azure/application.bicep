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

@allowed(['resend', 'azure', 'smtp'])
param emailProvider string
param azureEmailFromEmail string = ''
param smtpHost string = 'smtp.gmail.com'
param smtpPort int = 587
param smtpUsername string = ''

@description('Public HTTPS recovery handoff, for example https://<app-host>/authentication/reset-password.')
param passwordRecoveryUrl string

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
    emailProvider: emailProvider
    azureEmailConnectionString: emailProvider == 'azure' ? vault.getSecret('azure-email-connection-string') : ''
    azureEmailFromEmail: azureEmailFromEmail
    smtpHost: smtpHost
    smtpPort: smtpPort
    smtpUsername: smtpUsername
    smtpPassword: emailProvider == 'smtp' ? vault.getSecret('smtp-password') : ''
    passwordRecoveryUrl: passwordRecoveryUrl
    cloudinaryCloudName: vault.getSecret('cloudinary-cloud-name')
    cloudinaryApiKey: vault.getSecret('cloudinary-api-key')
    cloudinaryApiSecret: vault.getSecret('cloudinary-api-secret')
  }
}

output apiUrl string = runtime.outputs.apiUrl
