targetScope = 'resourceGroup'

param communicationServiceName string = 'acs-ecomind-2c297d64'
param workspaceName string = 'log-ecomind-prod-westus2'

resource communication 'Microsoft.Communication/communicationServices@2025-09-01' existing = {
  name: communicationServiceName
}

resource workspace 'Microsoft.OperationalInsights/workspaces@2023-09-01' existing = {
  name: workspaceName
}

resource diagnostics 'Microsoft.Insights/diagnosticSettings@2021-05-01-preview' = {
  name: 'email-delivery'
  scope: communication
  properties: {
    workspaceId: workspace.id
    logAnalyticsDestinationType: 'Dedicated'
    logs: [
      { category: 'EmailSendMailOperational', enabled: true }
      { category: 'EmailStatusUpdateOperational', enabled: true }
    ]
  }
}
