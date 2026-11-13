targetScope = 'resourceGroup'

@allowed(['westus2', 'centralus'])
@description('PostgreSQL and Container Apps must use the same region. Allocation is verified during provisioning.')
param location string = 'westus2'

@description('Keep this region unchanged after creating the vault.')
param vaultLocation string = 'westus2'

param resourceSuffix string = '2c297d64'

@description('Create only for initial database setup. Set false after the completed job is removed.')
param deployBootstrapJob bool = true

@secure()
@minLength(16)
@maxLength(128)
@description('New PostgreSQL administrator password. Enter privately and save in your password manager.')
param administratorPassword string

@secure()
@minLength(24)
@maxLength(128)
@description('Different password for the restricted ecomind_app database login.')
param applicationDatabasePassword string

@secure()
@minLength(64)
@description('Separate random signing key, at least 64 characters. Save privately; never use an example value.')
param jwtSigningKey string

@description('Pinned PostgreSQL client image; contains the CA trust bundle for verify-full.')
param bootstrapImage string = 'postgres:17-alpine@sha256:e5d2edd28b1f8d40a0a6c0c9cdc545b19b56a0d5d88f94bc11a97d970d4700d0'

var tags = {
  project: 'EcoMind'
  environment: 'production'
}
var serverName = 'psql-ecomind-${resourceSuffix}-${location}'
var vaultName = 'kv-ecomind-${resourceSuffix}'

module network './network.bicep' = {
  name: 'ecomind-network-${location}'
  params: {
    location: location
    virtualNetworkName: 'vnet-ecomind-prod-${location}'
    tags: tags
  }
}

resource vault 'Microsoft.KeyVault/vaults@2024-11-01' = {
  name: vaultName
  location: vaultLocation
  tags: tags
  properties: {
    tenantId: tenant().tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 30
    enablePurgeProtection: true
    enabledForTemplateDeployment: true
    enabledForDeployment: false
    enabledForDiskEncryption: false
    publicNetworkAccess: 'Enabled'
    accessPolicies: []
  }
}

resource administratorSecret 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'db-admin-password'
  properties: {
    value: administratorPassword
    contentType: 'PostgreSQL administrator password'
  }
}

resource applicationSecret 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'db-app-password'
  properties: {
    value: applicationDatabasePassword
    contentType: 'PostgreSQL application password'
  }
}

resource signingSecret 'Microsoft.KeyVault/vaults/secrets@2024-11-01' = {
  parent: vault
  name: 'jwt-signing-key'
  properties: {
    value: jwtSigningKey
    contentType: 'JWT signing key'
  }
}

resource dns 'Microsoft.Network/privateDnsZones@2024-06-01' = {
  name: 'private.postgres.database.azure.com'
  location: 'global'
  tags: tags
}

resource dnsLink 'Microsoft.Network/privateDnsZones/virtualNetworkLinks@2024-06-01' = {
  parent: dns
  name: 'ecomind-${location}'
  location: 'global'
  properties: {
    registrationEnabled: false
    virtualNetwork: {
      id: network.outputs.virtualNetworkId
    }
  }
}

resource databaseServer 'Microsoft.DBforPostgreSQL/flexibleServers@2025-08-01' = {
  name: serverName
  location: location
  tags: tags
  sku: {
    name: 'Standard_B1ms'
    tier: 'Burstable'
  }
  properties: {
    createMode: 'Default'
    version: '17'
    administratorLogin: 'ecomind_admin'
    administratorLoginPassword: administratorPassword
    authConfig: {
      activeDirectoryAuth: 'Disabled'
      passwordAuth: 'Enabled'
    }
    storage: {
      type: 'Premium_LRS'
      storageSizeGB: 32
      autoGrow: 'Disabled'
      tier: 'P4'
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: 'Disabled'
    }
    highAvailability: {
      mode: 'Disabled'
    }
    network: {
      publicNetworkAccess: 'Disabled'
      delegatedSubnetResourceId: '${network.outputs.virtualNetworkId}/subnets/snet-postgresql'
      privateDnsZoneArmResourceId: dns.id
    }
  }
  dependsOn: [dnsLink]
}

resource database 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2025-08-01' = {
  parent: databaseServer
  name: 'ecomind'
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

resource logs 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: 'log-ecomind-prod-${location}'
  location: location
  tags: tags
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
    workspaceCapping: {
      dailyQuotaGb: json('0.1')
    }
    features: {
      enableLogAccessUsingOnlyResourcePermissions: true
    }
  }
}

resource environment 'Microsoft.App/managedEnvironments@2025-07-01' = {
  name: 'cae-ecomind-prod-${location}'
  location: location
  tags: tags
  properties: {
    zoneRedundant: false
    vnetConfiguration: {
      infrastructureSubnetId: '${network.outputs.virtualNetworkId}/subnets/snet-container-apps'
      internal: false
    }
    workloadProfiles: [
      {
        name: 'Consumption'
        workloadProfileType: 'Consumption'
      }
    ]
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logs.properties.customerId
        sharedKey: logs.listKeys().primarySharedKey
      }
    }
  }
}

resource bootstrap 'Microsoft.App/jobs@2025-07-01' = if (deployBootstrapJob) {
  name: 'job-ecomind-db-bootstrap'
  location: location
  tags: tags
  properties: {
    environmentId: environment.id
    workloadProfileName: 'Consumption'
    configuration: {
      triggerType: 'Manual'
      replicaTimeout: 180
      replicaRetryLimit: 0
      manualTriggerConfig: {
        parallelism: 1
        replicaCompletionCount: 1
      }
      secrets: [
        { name: 'db-admin-password', value: administratorPassword }
        { name: 'db-app-password', value: applicationDatabasePassword }
      ]
    }
    template: {
      containers: [
        {
          name: 'bootstrap'
          image: bootstrapImage
          command: ['/bin/sh', '-c']
          args: [loadTextContent('bootstrap-database.sh')]
          resources: {
            cpu: json('0.25')
            memory: '0.5Gi'
          }
          env: [
            { name: 'PGHOST', value: databaseServer.properties.fullyQualifiedDomainName }
            { name: 'PGPORT', value: '5432' }
            { name: 'PGDATABASE', value: database.name }
            { name: 'PGUSER', value: 'ecomind_admin' }
            { name: 'PGPASSWORD', secretRef: 'db-admin-password' }
            { name: 'APP_DATABASE_PASSWORD', secretRef: 'db-app-password' }
            { name: 'PGSSLMODE', value: 'verify-full' }
            { name: 'PGSSLROOTCERT', value: '/etc/ssl/certs/ca-certificates.crt' }
          ]
        }
      ]
    }
  }
  dependsOn: [database]
}

output databaseHost string = databaseServer.properties.fullyQualifiedDomainName
output environmentId string = environment.id
output keyVaultId string = vault.id
output bootstrapJobName string = deployBootstrapJob ? bootstrap.name : ''
