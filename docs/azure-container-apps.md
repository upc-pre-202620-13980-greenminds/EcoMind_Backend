# Azure Container Apps deployment

## Status and scope

As of November 12, 2026, the EcoMind subscription, resource group and virtual network
have been created and verified. The Basic registry has also been created, with its
administrator account disabled. PostgreSQL B1ms allocation in East US 2 failed with
`SkuNotAvailable`, despite appearing in the capability catalog. No server was created.
The foundation template prepares a retry in West US 2 at the same B1ms reference
compute/storage price. The West US 2 foundation deployment completed on November 12. PostgreSQL is Ready
and private, the Consumption environment is provisioned, and the bootstrap job
connected as `ecomind_app` to `ecomind` using TLS 1.3 with `verify-full`. The API
deployment is prepared in the portal but is not yet submitted. API runtime and real
email validation remain pending.
Use the Azure portal for the first resource creation and capture the actual steps for
the course report. The 202620 V4.0 assignment, page 29, requires deployment screenshots
and explanations; page 25 requires reproducible configuration and a C4 deployment view.
CLI validation and later maintenance complement those screenshots.
The C4 deployment update and sanitized resource configuration export are pending until
the actual Azure environment has been created and verified.

This feature starts from `develop` at `f0f5051`. The IAM email improvements in backend
PR #30 must also be integrated before validating the final recovery email experience.
Keep feature work on `feature/*`, integrate through `develop`, and release through the
team's existing `release/*` / `main` flow. Resource creation does not authorize a release.

## CLI session

Azure CLI is installed through Homebrew. Use the project wrapper to isolate its login:

```bash
bash scripts/azure.sh login
bash scripts/azure.sh account list --query '[].{name:name,id:id,state:state}' -o table
bash scripts/azure.sh account set --subscription '<subscription-id>'
bash scripts/azure.sh extension add --name containerapp
```

Verify the selected account and subscription before writes. Do not store passwords,
API keys, client secrets or access tokens in Git, screenshots, shell history or logs.
Use interactive entry or a local password manager for credentials. The account login
and any subscription activation remain owner actions.

## Resource configuration

Use the existing resource group, `rg-ecomind-prod`, in East US 2, tagged `project=EcoMind`
and `environment=production`. PostgreSQL provisioning is restricted in East US for this
subscription; East US 2 lists B1ms but actual provisioning failed. The next allocation
attempt is West US 2. Keep PostgreSQL and Container Apps on the same regional VNet.
The catalog and template validation do not guarantee allocation. Do not silently switch to a
larger tier if B1ms or Consumption is unavailable.

| Resource | Initial configuration |
| --- | --- |
| Virtual network | `vnet-ecomind-prod`, `10.42.0.0/16` |
| Container Apps subnet | `snet-container-apps`, `10.42.0.0/23`, delegated to `Microsoft.App/environments` |
| PostgreSQL subnet | `snet-postgresql`, `10.42.2.0/28`, delegated to `Microsoft.DBforPostgreSQL/flexibleServers` |
| PostgreSQL Flexible Server | Unique name, PostgreSQL 17, Burstable B1ms, 32 GiB storage, 7-day local backup retention, no HA |
| PostgreSQL database | `ecomind`, UTF-8; new database, separate from the current Aiven data |
| PostgreSQL network | Private VNet integration; link the private DNS zone ending in `.postgres.database.azure.com` to this VNet |
| Container Registry | Unique name, Basic, admin user disabled |
| Image pull identity | User-assigned managed identity, `AcrPull` scoped only to this registry |
| Container Apps environment | `cae-ecomind-prod`, workload profiles environment, only the Consumption profile, external ingress, existing VNet/subnet |
| Logs | Log Analytics, 30-day retention, sampling / restrained log levels and an ingestion cap |
| Container App | `ca-ecomind-api`, Linux AMD64, 0.5 vCPU / 1 GiB, minimum 0 / maximum 2 replicas, single active revision |
| Public ingress | HTTPS only, target port 8080, HTTP scaling rule |

The database must remain private. Do not add a public PostgreSQL firewall rule for all
Azure services or the internet. The Container Apps environment needs outbound access
to the registry, Resend and platform dependencies; validate that connectivity before
adding restrictive network rules. A NAT gateway, private endpoint, dedicated workload
profile and paid support are outside this small deployment estimate.

B1ms and one application replica are starting sizes, not a performance guarantee.
Validate memory, database connections and startup duration with the deployed app.
Keep the database on a currently supported PostgreSQL version.

The network created in the portal is reproduced by `infra/azure/network.bicep`.
Both subnets have `defaultOutboundAccess=false`. Validate managed environment egress
to the registry and mail provider before accepting the deployment. This file covers
only the network; it is not a complete application deployment template. Before any
future network update, export the current configuration and review the change preview
so that attached services and later subnet changes are preserved:

```bash
bash scripts/azure.sh deployment group what-if -g rg-ecomind-prod \
  --template-file infra/azure/network.bicep
```

## Reproducible portal deployment

`infra/azure/foundation.bicep` creates a regional VNet, private PostgreSQL 17 B1ms server
and empty `ecomind` database, Key Vault, Log Analytics and a Consumption environment.
It also creates a manual database bootstrap job. The three credential parameters are
`securestring` values with no defaults. The owner enters them privately in the portal:
a PostgreSQL administrator password, a different application database password and a
separate random JWT signing key of at least 64 characters. Save all three in a password
manager. Do not paste real values into a parameter file, ticket, screenshot or chat.
The template stores them in the project Key Vault so a CLI maintenance retry can use
ARM Key Vault parameter references without revealing their values. Keep `vaultLocation` unchanged
once the vault exists. Do not delete the vault or resource group to retry a region;
recover a soft-deleted vault instead of trying to reuse its protected name. Soft-delete retention is 30 days and purge protection is enabled.

1. Compile the foundation template with `bash scripts/azure.sh bicep build --file
   infra/azure/foundation.bicep --outfile /tmp/ecomind-foundation.json`.
2. In the portal, choose **Deploy a custom template**, load that JSON in the editor,
   select the existing project subscription/resource group and inspect the resource
   preview. The owner completes the three secure fields and submits the deployment.
   Capture configuration and results while keeping all credential values hidden.
3. Verify PostgreSQL is ready and private. Start `job-ecomind-db-bootstrap` through the
   CLI. Wait for a successful execution and inspect its non-secret result: the
   `ecomind_app` login must connect to `ecomind` with TLS. The job uses `verify-full`
   and the image CA trust bundle. Never weaken that setting to fix a connection issue.
4. The bootstrap grants the application login `CONNECT` and `USAGE, CREATE` on the
   empty database's public schema. This allows the current Hibernate schema updates
   without giving the API superuser, role administration or database creation rights.
   This job does not migrate Aiven data. Review ownership/grants separately for a restore.
5. After bootstrap succeeds, compile `infra/azure/application.bicep` and create the API
   from that template in the portal. Supply the verified registry digest. This wrapper
   resolves `db-app-password` and `jwt-signing-key` through `getSecret` into secure
   parameters of `application-resources.bicep`; the portal form has no password fields.
   ARM resolves the values server-side into native Container Apps secret references.
   The runtime never receives the administrator password. The managed identity receives
   only `AcrPull` at this registry's scope. Keep `configureEmail=false` until a real
   `resend-api-key` exists in the vault and `resendFromEmail` is a verified sender.
6. Remove the completed bootstrap job after validating the application login and keep
   its sanitized execution evidence. Set `deployBootstrapJob=false` for later foundation
   deployments so maintenance does not recreate the administrator job. It contains the administrator secret and should
   not remain available for routine application operation. Keep the Key Vault copy for
   authorized maintenance. A signing-key rotation requires restarting/revising the API
   and invalidates existing sessions.

The portal Custom Deployment form discarded root-level Key Vault parameter references
in an observed attempt, then reported empty required password fields. The application
wrapper avoids that UI limitation by binding vault references directly in nested module
parameters. Use this wrapper in the portal. The deploying owner needs
`Microsoft.KeyVault/vaults/deploy/action`, provided by the subscription Owner role.
Directly reading a secret in the Key Vault portal is a separate data-plane permission
and is not required for these deployment references. Do not grant a runtime identity
vault-wide access just to work around the form.

An `AcrPull` assignment can take time to propagate after its creation. If the initial
revision fails specifically with an authorization error, verify registry scope and ARM
token authentication, allow propagation and retry the same tested digest. Do not enable
registry administrator passwords.

Example ARM parameter reference for CLI maintenance, with no secret value:

```json
{
  "applicationDatabasePassword": {
    "reference": {
      "keyVault": { "id": "<project-key-vault-resource-id>" },
      "secretName": "db-app-password"
    }
  }
}
```

Both templates compile locally. The foundation passed ARM validation and change preview
using explicitly non-live validation values. Those checks do not create resources or
prove regional capacity, DNS, egress, certificate validation, startup or email delivery.
The SQL bootstrap passed twice against an isolated local PostgreSQL 17 instance,
including a password with quotes and rejection of database creation by the app login.
That local SQL check does not validate Azure TLS.

## Image

Build the clean production image with `Dockerfile.azure`. Its context excludes the
existing development properties file and local credentials through the build-specific
`Dockerfile.azure.dockerignore`; the existing Render build context is preserved. Use
BuildKit (`docker buildx`). It runs as a non-root user and requires runtime secrets;
payment credentials default to empty values in `prod`, and no live credentials are baked
into this image. Base images are pinned by digest; update those pins deliberately for
security maintenance.

```bash
test -z "$(git status --porcelain)" || { echo 'Build from a clean checkout.'; exit 1; }
commit=$(git rev-parse HEAD)
docker buildx build --platform linux/amd64 -f Dockerfile.azure \
  --build-arg GIT_COMMIT="$commit" \
  -t "<registry>.azurecr.io/ecomind-backend:$commit" --load .
```

Run the repository's existing H2/PostgreSQL CI before deploying that exact commit.
Authenticate to the registry using the selected Azure account, push the immutable tag,
and select the pushed image digest in the portal. Record both commit and image digest.
Do not push a feature branch as a production release or
deploy an image built from a different commit than the one recorded in the evidence.
Do not enable registry administrator passwords.

## Application settings

In the portal, create the Container App secrets before referring to them as environment
variables. Use secret references for passwords, signing keys and API keys. Assign
`AcrPull` to the image pull identity at registry scope and select that identity for pulls.

| Variable | Value / source |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `prod,azure` |
| `PORT` | `8080` |
| `DATABASE_URL` | `jdbc:postgresql://<server-fqdn>:5432/ecomind?sslmode=verify-full&sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory&currentSchema=public` |
| `DATABASE_USERNAME` | Database login configured for EcoMind |
| `DATABASE_PASSWORD` | Secret reference |
| `JWT_SECRET` | Secret reference; strong newly generated signing key |
| `JWT_EXPIRATION_MINUTES` | `60` |
| `RESEND_API_KEY` | Secret reference from the team's Resend account |
| `RESEND_FROM_EMAIL` | Address on the verified sender domain |
| `PASSWORD_RECOVERY_URL` | `ecomind://authentication/reset-password` for the current Android implementation |
| `CULQI_SECRET_KEY`, `PAYPAL_CLIENT_ID`, `PAYPAL_SECRET` | Only the team's approved sandbox configuration, when required |

The `azure` profile is used together with `prod`; it limits the database pool to five
connections per replica and applies graceful shutdown. Never enable `dev` in Azure.
The JDBC SSL factory uses the JRE truststore; verify that it trusts the Azure server
certificate chain and validates its hostname against the actual server. Do not disable
certificate verification to bypass a connection failure.
New signing keys invalidate old sessions, so preserve an existing key only through an
authorized secure transfer if continuity is needed.

Do not capture secret values. Without a verified Resend domain, test sending is limited
to the Resend account owner's email. Hosting alone does not enable mail delivery.

## Probes and scaling

Configure startup and liveness TCP probes on port 8080. Allow up to 300 seconds for
startup while measuring the actual Java startup time; do not declare the API ready
merely because a deployment operation finished. Configure an HTTP readiness probe on
`/v3/api-docs` on port 8080 and verify it returns 200 before shifting traffic.
This is an API startup check, not a database health endpoint. Confirm that springdoc
is exposed under `prod,azure` and perform a database-backed request separately.

Keep minimum replicas at 0 for normal low use. Use minimum 1 temporarily for a planned
review or demo, then return to 0. The first request after suspension includes startup
latency. Background work inside an inactive API cannot run reliably; scheduled reminders
need a separate trigger/job if that feature is included in the release.
Check cold-start duration against the Android and Flutter client timeouts before
accepting scale-to-zero for their interactive flows.

## Validation and evidence

After the portal shows a ready revision:

```bash
bash scripts/azure.sh containerapp show -g rg-ecomind-prod -n ca-ecomind-api \
  --query '{state:properties.provisioningState,fqdn:properties.configuration.ingress.fqdn,minReplicas:properties.template.scale.minReplicas,maxReplicas:properties.template.scale.maxReplicas}'
python3 scripts/verify-deployment.py 'https://<app-fqdn>'
```

Capture the resource group, VNet/subnets, PostgreSQL compute/storage/private network,
registry and managed identity selection, application image/port/scaling/probes,
deployment result, Swagger and one authenticated mobile journey. Include URLs, exact
commit, date, configuration, observable outcome and an explanation in the report.
Take screenshots during the actual steps; never reconstruct a successful deployment.

Measure the first request after suspension and repeated requests while running. Check
database persistence after a new revision, restore a backup to a separate temporary
server only after accounting for its extra charge, and test recovery email with a real
inbox before calling the IAM flow validated. Validate Android and Flutter base URLs
against the same API. Do not replace the existing Render URL until Azure passes these
checks and the team is ready to switch.
The verification script checks OpenAPI exposure, one expected path and rejection of
an unauthenticated request. It does not prove that all business endpoints work or that
the protected test route exists; a security filter may reject it before routing.

## Cost and operations

Initial planning range: USD 22–30/month before taxes for very light usage, assuming
the Container Apps monthly free grant covers most API execution, PostgreSQL B1ms
running for 730 hours, 32 GiB storage and ACR Basic. Private DNS and logs may add
charges. The subscription budget `ecomind-monthly-30` is configured for USD 30 per month
with actual-cost alerts at 50%, 80% and 100%. A budget alert is a notification, not a
spending cap. The East US 2 portal estimate for PostgreSQL B1ms / 32 GiB was USD 16.09 per month.
The Microsoft Retail API lists West US 2 B1ms at USD 0.017/hour and storage at
USD 0.115/GB-month, giving the same planning total at 730 hours. Key Vault operations,
DNS, registry, logs, cross-region image pulls and taxes are additional.

There is no high availability in this budget. Size upgrades, excess traffic/logs,
additional backup storage, email/domain charges and demo time with minimum 1 can
increase it. Review actual cost after the first few days. Do not enable paid Defender
plans, NAT Gateway, HA, geo-replication or duplicate environments by default.

Automatic production CD is not enabled by this feature. After the subscription exists,
configure a GitHub production environment and a federated identity scoped to this
resource group / registry. Use OIDC rather than a long-lived Azure client secret. Keep
the existing CI checks and require the exact tested image for releases. Capture that
actual setup before documenting CD as enabled.

The existing Aiven database is not migrated by creating an empty Azure database.
Preserving accounts and progress requires an authorized export, compatible restore,
validation and a coordinated cutover. Keep the original service available until then.

## References

- Microsoft: [Java on Container Apps](https://learn.microsoft.com/azure/container-apps/java-overview).
- Microsoft: [VNet integration](https://learn.microsoft.com/azure/container-apps/vnet-custom).
- Microsoft: [Private PostgreSQL networking](https://learn.microsoft.com/azure/postgresql/network/concepts-networking-private).
- Microsoft: [Managed identity image pulls](https://learn.microsoft.com/azure/container-apps/managed-identity-image-pull).
- Microsoft: [Health probes](https://learn.microsoft.com/azure/container-apps/health-probes).
- Microsoft: [Consumption billing](https://learn.microsoft.com/azure/container-apps/billing).
