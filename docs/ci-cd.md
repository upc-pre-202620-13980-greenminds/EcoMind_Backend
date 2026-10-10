# Backend delivery workflow

## Continuous integration

`.github/workflows/backend.yml` runs for pull requests targeting `develop` or `main`, pushes to those branches, manual dispatches, and `v*` release tags. Release tags must have the form `vMAJOR.MINOR.PATCH` and match the project version in `pom.xml`. Java 21 and the Maven Wrapper compile the service and execute `clean verify` with two database configurations: H2 in PostgreSQL compatibility mode and PostgreSQL 17.

Both configurations execute the unit, integration and Cucumber acceptance suites. A failed test or coverage below 85% of lines or 65% of branches fails the job. Each execution retains Surefire XML, Cucumber HTML/JSON/JUnit reports and JaCoCo coverage for 14 days. The PostgreSQL job also uploads the executable JAR. The container job builds the existing deployment Dockerfile only after both verification jobs pass. Pull requests do not deploy.

The workflow validates Conventional Commit pull request titles. Feature work targets `develop`; release and hotfix branches target `main`. Release versions use `MAJOR.MINOR.PATCH` and are updated in `pom.xml` before creating the release tag. Dependency updates for Maven and GitHub Actions are proposed through Dependabot pull requests and follow the same checks.

## Continuous deployment

The production deployment job runs only after successful verification and container build for a push to `main`. It is disabled unless the repository variable `RENDER_DEPLOY_ENABLED` equals `true`.

The service administrator configures:

1. A Git-backed Render service pointing to this repository, with the existing `Dockerfile` and `main` branch.
2. Automatic deploys disabled in Render, so unverified commits do not bypass the workflow.
3. The `production` GitHub environment, with the secret `RENDER_DEPLOY_HOOK_URL` containing that service's deploy hook.
4. The repository variable `RENDER_DEPLOY_ENABLED=true`.

The job requests the exact verified commit using Render's `ref` parameter. It accepts only an HTTPS Render hook. A missing hook or rejected request fails the job; credentials are never stored in source control. HTTP 200/202 confirms that Render accepted the request. Deployment completion and runtime health are verified in the Render service's deployment logs.

Production database, JWT, email and payment credentials remain in the service configuration. Tests use isolated local credentials and do not require access to production or external payment providers.

## Local verification

```bash
bash ./mvnw --batch-mode --no-transfer-progress clean verify
```

For PostgreSQL, create a disposable test database and supply its connection:

```bash
export TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/ecomind_test
export TEST_DATABASE_DRIVER=org.postgresql.Driver
export TEST_DATABASE_USERNAME=ecomind_test
export TEST_DATABASE_PASSWORD=test-only-password
bash ./mvnw --batch-mode --no-transfer-progress clean verify
```

The test profile recreates its database tables with `create-drop`; use a dedicated test database. Reports are written under `target/surefire-reports`, `target/cucumber` and `target/site/jacoco`.

## Required checks

The merge checks are `Pull request conventions`, `Tests (h2)`, `Tests (postgresql)` and `Build deployment image`. Repository administrators can select those checks in branch protection for `develop` and `main`. The workflow has read-only repository permissions and does not merge pull requests.

## References

- [GitHub Actions: Java with Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven)
- [Render deploy hooks](https://render.com/docs/deploy-hooks)
- [JaCoCo Maven agent](https://www.jacoco.org/jacoco/trunk/doc/prepare-agent-mojo.html)
