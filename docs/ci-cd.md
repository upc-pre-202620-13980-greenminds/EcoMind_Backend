# Backend delivery workflow

## Branches and checks

Work starts in `feature/*` and is integrated by pull request into `develop`. Releases
and hotfixes target `main` through the existing GitFlow. Use Conventional Commit titles.
Pull requests targeting either branch run all checks without Azure credentials.

Java 21 and the Maven Wrapper run `clean verify` against H2 and PostgreSQL 17. Unit,
integration and Cucumber suites must pass, with at least 85% line and 65% branch
coverage. The deployment helper's isolated tests run before Maven. Existing thresholds
and tests remain mandatory. Both database jobs retain test/coverage reports for 14 days;
the PostgreSQL job also retains the executable JAR.

After both jobs pass, the container job builds `Dockerfile.azure` for Linux AMD64. It
rejects packaged development/local configuration, scans OS/Java dependencies with a
pinned Trivy release and fails on fixable HIGH/CRITICAL vulnerabilities. The JSON scan
report is retained for 14 days, including failed scans. The retained report is filtered to fixable HIGH/CRITICAL findings;
it is not a complete inventory of unfixed or lower-severity findings. Dependabot proposes weekly
Maven, action and Docker updates into `develop`; updates use the same checks.
The initial scan found fixable runtime dependency findings. Spring Boot was updated
within 4.0.x to 4.0.9, and HttpCore to the fixed 5.4.3 release; the gate was retained.

## Automatic Azure deployment

Only `push` events on `develop`, including merged pull requests, deploy when
`AZURE_DEPLOY_ENABLED=true`. PRs, `main`, tags and manual workflow dispatches do not
deploy this shared environment. Direct pushes to `develop` also trigger the workflow;
branch protection must require PRs to prevent that route.

The tested/scanned image is exported once and retained for one day. The deployment job:

1. Skips a commit superseded by a newer `develop` head.
2. Downloads that run's image, verifies its source SHA, Linux AMD64 platform and
   non-root user `10001:10001`; it does not rebuild application code.
3. Authenticates with OIDC and publishes to the existing Azure Container Registry,
   using a commit/run/attempt tag.
4. Updates only the existing `ca-ecomind-api` container image, pinned by SHA256 digest.
   Existing secrets, identity, network, probes, database and 0–2 scaling stay configured.
5. Waits for the exact expected revision and image to be active, Healthy and Provisioned,
   then checks OpenAPI 200 and anonymous account access 401. An older serving revision
   cannot satisfy this check. The final ready revision is checked again after HTTP probes.

Deployments are serialized and are not canceled halfway through an update. The summary
records the image digest, ready revision, previous revision and public URL. A pipeline
passing compilation alone is not recorded as a completed deployment.

## Authentication and infrastructure

`infra/azure/github-deploy.bicep` creates the separate user-assigned identity
`id-ecomind-github-deploy`. Its federated credential trusts exactly
`repo:upc-pre-202620-13980-greenminds/EcoMind_Backend:ref:refs/heads/develop`, issued by
`https://token.actions.githubusercontent.com` for `api://AzureADTokenExchange`.
There is no Azure client secret. Only the deployment job receives `id-token: write`.

GitHub repository secrets `AZURE_CLIENT_ID`, `AZURE_TENANT_ID` and
`AZURE_SUBSCRIPTION_ID` hold its identifiers. Runtime database, JWT, mail and provider
credentials remain in Azure; the workflow does not read or replace them.

The custom deployment role permits app read/write/revision read and registry metadata
read. It is assigned only to this Container App and this registry. `AcrPush` is assigned
only to this registry. The identity has no Key Vault, PostgreSQL, network or role-management
assignment. The app's separate pull identity retains its existing `AcrPull` assignment.

A subscription administrator can reproduce identity/role setup with:

```bash
bash scripts/azure.sh deployment group validate -g rg-ecomind-prod --template-file infra/azure/github-deploy.bicep
bash scripts/azure.sh deployment group what-if -g rg-ecomind-prod --template-file infra/azure/github-deploy.bicep
bash scripts/azure.sh deployment group create -g rg-ecomind-prod --template-file infra/azure/github-deploy.bicep
```

Configure the three output identifiers as GitHub secrets and set repository variable
`AZURE_DEPLOY_ENABLED=true`. The resource names/registry host in the workflow identify
this existing shared environment. There is no second paid deployment environment.

## Failure and recovery

A test, coverage, image configuration or vulnerability-gate failure prevents deployment.
An update already in progress outside this pipeline must finish before another begins.
If rollout or public smoke checks fail, the helper restores and verifies the previous
image only when the failed attempt still owns the current image/revision. It leaves a
subsequent external update alone. A restored rollout still fails the GitHub job and
reports recovery; it does not mark the attempted version successful.

Image rollback does not reverse database schema/data changes or external side effects.
Keep schema changes backward compatible and review their recovery plan in the PR. The
existing PostgreSQL backup/PITR configuration is separate; restore requires an authorized
operator and a verified restore point. Do not point tests at the application database.

To pause deployments while keeping CI active:

```bash
gh variable set AZURE_DEPLOY_ENABLED --body false
```

Investigate the failed GitHub job and its retained test/security reports first. Use the
project Azure CLI wrapper to read app/revision state and Azure logs; keep raw application
logs private. Never print secrets, recovery links or access tokens. Once the cause is
fixed, merge a new tested commit and re-enable the variable. Rerunning an old commit
superseded by `develop` will not deploy it.

For an operator-driven image rollback, use a previously verified digest from the job
summary and preserve the current application configuration:

```bash
bash scripts/azure.sh containerapp update -g rg-ecomind-prod -n ca-ecomind-api --container-name ecomind-api --image '<registry>/ecomind-backend@sha256:<verified-digest>' --output none
python3 scripts/verify-deployment.py 'https://<app-fqdn>'
```

Confirm the exact ready revision and image as well as HTTP health. Existing Azure logs,
metrics, retention/cap and cost budget remain in use. GitHub run failures are visible in
Actions; personal notification preferences are managed by each team member.

## Merge protection and releases

A repository administrator should require `Pull request conventions`, `Tests (h2)`,
`Tests (postgresql)` and `Build deployment image`, require an up-to-date PR, resolve
conversations and disallow force pushes/deletions on `develop` and `main`. The deployment
job is not a PR check: it runs after merging. Configuration of the workflow alone does
not prove these repository protection settings are enabled. The current personal
account has repository write access, not administration; its protection update request
was rejected. `infra/github/branch-protection.json` is the prepared administrator
payload. An administrator can apply it to both branches:

```bash
gh api --method PUT repos/upc-pre-202620-13980-greenminds/EcoMind_Backend/branches/develop/protection --input infra/github/branch-protection.json
gh api --method PUT repos/upc-pre-202620-13980-greenminds/EcoMind_Backend/branches/main/protection --input infra/github/branch-protection.json
```

The payload requires a PR and successful current checks, disallows forced updates and
deletion, and resolves conversations. It does not add a mandatory approval count.

Release tags use `vMAJOR.MINOR.PATCH` and must match `pom.xml`. Tags and `main` run the
same verification and retain artifacts, without replacing the `develop` Azure image.
The old Render deploy hook is no longer used by this workflow.

## Local verification

```bash
python3 -m unittest discover -s scripts -p 'test_*.py'
bash ./mvnw --batch-mode --no-transfer-progress clean verify
```

PostgreSQL tests use `TEST_DATABASE_URL`, `TEST_DATABASE_DRIVER`,
`TEST_DATABASE_USERNAME` and `TEST_DATABASE_PASSWORD` for an isolated disposable database.
The test profile recreates tables with `create-drop`.

## References

- [Azure Login with OIDC](https://learn.microsoft.com/azure/developer/github/connect-from-azure-openid-connect)
- [Federated managed identity](https://learn.microsoft.com/entra/workload-id/workload-identity-federation-create-trust-user-assigned-managed-identity)
- [Azure Container Apps revisions](https://learn.microsoft.com/azure/container-apps/revisions)
- [Trivy action](https://github.com/aquasecurity/trivy-action)
- [GitHub protected branches](https://docs.github.com/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches)
