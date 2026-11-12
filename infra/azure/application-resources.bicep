targetScope = 'resourceGroup'

@allowed(['westus2', 'centralus'])
param location string = 'westus2'
param resourceSuffix string = '2c297d64'
param registryName string = 'acrecomind2c297d64'

@description('Use the verified immutable image digest from the existing private registry.')
@minLength(71)
@maxLength(71)
param imageDigest string

@secure()
@description('Resolved from Key Vault by the parent deployment.')
param applicationDatabasePassword string

@secure()
@minLength(64)
@description('Resolved from Key Vault by the parent deployment.')
param jwtSigningKey string

@secure()
@description('Leave empty until the team provides a real Resend key; email remains unvalidated.')
param resendApiKey string = ''
param resendFromEmail string = ''

@allowed(['resend', 'azure'])
param emailProvider string = 'resend'
@secure()
param azureEmailConnectionString string = ''
param azureEmailFromEmail string = ''
param passwordRecoveryUrl string

var tags = {
  project: 'EcoMind'
  environment: 'production'
}

resource registry 'Microsoft.ContainerRegistry/registries@2025-04-01' existing = {
  name: registryName
}

resource environment 'Microsoft.App/managedEnvironments@2025-07-01' existing = {
  name: 'cae-ecomind-prod-${location}'
}

resource databaseServer 'Microsoft.DBforPostgreSQL/flexibleServers@2025-08-01' existing = {
  name: 'psql-ecomind-${resourceSuffix}-${location}'
}

resource identity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = {
  name: 'id-ecomind-api-pull'
  location: location
  tags: tags
}

resource imagePullRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(registry.id, identity.id, 'AcrPull')
  scope: registry
  properties: {
    principalId: identity.properties.principalId
    principalType: 'ServicePrincipal'
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '7f951dda-4ed3-4680-a7ca-43fe172d538d')
  }
}

resource app 'Microsoft.App/containerApps@2025-07-01' = {
  name: 'ca-ecomind-api'
  location: location
  tags: tags
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${identity.id}': {}
    }
  }
  properties: {
    managedEnvironmentId: environment.id
    workloadProfileName: 'Consumption'
    configuration: {
      activeRevisionsMode: 'Single'
      ingress: {
        external: true
        allowInsecure: false
        targetPort: 8080
        transport: 'auto'
        traffic: [{ latestRevision: true, weight: 100 }]
      }
      registries: [{ server: registry.properties.loginServer, identity: identity.id }]
      secrets: concat([
        { name: 'db-password', value: applicationDatabasePassword }
        { name: 'jwt-secret', value: jwtSigningKey }
      ], empty(resendApiKey) ? [] : [{ name: 'resend-api-key', value: resendApiKey }],
        empty(azureEmailConnectionString) ? [] : [{ name: 'azure-email-connection-string', value: azureEmailConnectionString }])
    }
    template: {
      terminationGracePeriodSeconds: 30
      containers: [
        {
          name: 'ecomind-api'
          image: '${registry.properties.loginServer}/ecomind-backend@${imageDigest}'
          resources: {
            cpu: json('0.5')
            memory: '1Gi'
          }
          env: concat([
            { name: 'SPRING_PROFILES_ACTIVE', value: 'prod,azure' }
            { name: 'PORT', value: '8080' }
            { name: 'DATABASE_URL', value: 'jdbc:postgresql://${databaseServer.properties.fullyQualifiedDomainName}:5432/ecomind?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory&currentSchema=public' }
            { name: 'DATABASE_USERNAME', value: 'ecomind_app' }
            { name: 'DATABASE_PASSWORD', secretRef: 'db-password' }
            { name: 'JWT_SECRET', secretRef: 'jwt-secret' }
            { name: 'JWT_EXPIRATION_MINUTES', value: '60' }
            { name: 'RESEND_FROM_EMAIL', value: resendFromEmail }
            { name: 'EMAIL_PROVIDER', value: emailProvider }
            { name: 'AZURE_EMAIL_FROM_EMAIL', value: azureEmailFromEmail }
            { name: 'PASSWORD_RECOVERY_URL', value: passwordRecoveryUrl }
          ], empty(resendApiKey) ? [{ name: 'RESEND_API_KEY', value: '' }] : [{ name: 'RESEND_API_KEY', secretRef: 'resend-api-key' }],
            empty(azureEmailConnectionString) ? [] : [{ name: 'AZURE_EMAIL_CONNECTION_STRING', secretRef: 'azure-email-connection-string' }])
          probes: [
            {
              type: 'Startup'
              tcpSocket: { port: 8080 }
              periodSeconds: 5
              timeoutSeconds: 2
              failureThreshold: 60
            }
            {
              type: 'Liveness'
              tcpSocket: { port: 8080 }
              periodSeconds: 30
              timeoutSeconds: 5
              failureThreshold: 3
            }
            {
              type: 'Readiness'
              httpGet: { path: '/v3/api-docs', port: 8080, scheme: 'HTTP' }
              periodSeconds: 10
              timeoutSeconds: 3
              failureThreshold: 3
            }
          ]
        }
      ]
      scale: {
        minReplicas: 0
        maxReplicas: 2
        rules: [{ name: 'http-requests', http: { metadata: { concurrentRequests: '20' } } }]
      }
    }
  }
  dependsOn: [imagePullRole]
}

output apiUrl string = 'https://${app.properties.configuration.ingress.fqdn}'
