targetScope = 'resourceGroup'

@description('Region of the existing resource group.')
param location string = resourceGroup().location

param virtualNetworkName string = 'vnet-ecomind-prod'
param tags object = {}

// Shared network layout. Foundation sets the region and name explicitly.
resource network 'Microsoft.Network/virtualNetworks@2025-07-01' = {
  name: virtualNetworkName
  location: location
  tags: tags
  properties: {
    addressSpace: {
      addressPrefixes: ['10.42.0.0/16']
    }
    enableDdosProtection: false
    privateEndpointVNetPolicies: 'Disabled'
    encryption: {
      enabled: false
      enforcement: 'AllowUnencrypted'
    }
    subnets: [
      {
        name: 'snet-container-apps'
        properties: {
          addressPrefixes: ['10.42.0.0/23']
          defaultOutboundAccess: false
          privateEndpointNetworkPolicies: 'Disabled'
          privateLinkServiceNetworkPolicies: 'Enabled'
          delegations: [
            {
              name: 'Microsoft.App/environments'
              properties: {
                serviceName: 'Microsoft.App/environments'
              }
            }
          ]
        }
      }
      {
        name: 'snet-postgresql'
        properties: {
          addressPrefixes: ['10.42.2.0/28']
          defaultOutboundAccess: false
          privateEndpointNetworkPolicies: 'Disabled'
          privateLinkServiceNetworkPolicies: 'Enabled'
          delegations: [
            {
              name: 'Microsoft.DBforPostgreSQL/flexibleServers'
              properties: {
                serviceName: 'Microsoft.DBforPostgreSQL/flexibleServers'
              }
            }
          ]
        }
      }
    ]
  }
}

output virtualNetworkId string = network.id
