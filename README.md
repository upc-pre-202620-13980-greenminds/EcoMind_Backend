# EcoMind Backend

RESTful web services of the EcoMind platform, built with Spring Boot and Java.

## Requirements

- Java 21 (JDK)
- PostgreSQL 14 or later
- The Maven Wrapper included in this repository (no local Maven installation is needed)

## Project structure

The application is a single Spring Boot project organized by bounded context. Each context has its
own package with the `domain`, `application`, `infrastructure` and `interfaces` layers, and does not
depend on the internal classes or tables of the others.

| Package | Purpose |
|---|---|
| `shared` | Cross-cutting code: error handling, i18n, naming strategy, auditing and OpenAPI configuration |
| `iam`, `users`, `learning`, `quests`, `community`, `gamification`, `monetization` | Bounded contexts, added as they are implemented |

## Environment variables

Set the following variables in the environment before running the application. Keep mail credentials out of source control and screenshots.

| Variable | Profile | Description |
|---|---|---|
| `DATABASE_URL` | dev, prod | JDBC URL of the PostgreSQL database. See [Database URL](#database-url) |
| `DATABASE_USERNAME` | dev, prod | Database user |
| `DATABASE_PASSWORD` | dev, prod | Database password |
| `JWT_SECRET` | dev, prod | Secret used to sign access tokens. At least 32 characters |
| `JWT_EXPIRATION_MINUTES` | dev, prod | Minutes an access token is valid |
| `CULQI_SECRET_KEY` | optional | Enables CARD and YAPE gem-package payments |
| `PAYPAL_CLIENT_ID` | optional | Enables PayPal gem-package payments |
| `PAYPAL_SECRET` | optional | Enables PayPal gem-package payments |
| `RESEND_API_KEY` | Resend | API key of the Resend email service |
| `RESEND_FROM_EMAIL` | Resend | Sender address of the emails, verified in Resend |
| `PASSWORD_RECOVERY_URL` | prod, email | Optional recovery screen URL; defaults to `ecomind://authentication/reset-password`. The recovery token is appended as the `token` query parameter |
| `EMAIL_PROVIDER` | prod, email | `resend` (default) or `azure` |
| `AZURE_EMAIL_CONNECTION_STRING` | Azure email | Native secret reference resolved from Key Vault |
| `AZURE_EMAIL_FROM_EMAIL` | Azure email | Verified managed-domain sender |
| `PORT` | prod | HTTP port the application listens on |
| `SPRING_PROFILES_ACTIVE` | prod | Use `prod`; on Azure Container Apps use `prod,azure` |
| `CLOUDINARY_CLOUD_NAME` | dev, prod | Cloudinary cloud name for Community event images |
| `CLOUDINARY_API_KEY` | dev, prod | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | dev, prod | Cloudinary API secret |

Real email requires the credentials and sender of the selected provider. Empty required
settings fail startup. The recovery URL has the Android default
shown above and can be overridden explicitly.

### Database URL

The URL uses the PostgreSQL JDBC format. For a local database:

```
jdbc:postgresql://localhost:5432/ecomind
```

In production the connection must be encrypted and the schema must be stated explicitly:

```
jdbc:postgresql://<host>:5432/<database>?sslmode=require&currentSchema=<schema>
```

`sslmode=require` rejects connections that are not encrypted. `currentSchema` is the schema where
the tables are created and read; it must exist before the application starts (PostgreSQL creates
`public` by default). The user and the password are never part of the URL: they go in
`DATABASE_USERNAME` and `DATABASE_PASSWORD`.

## Profiles

| Profile | Use | Notes |
|---|---|---|
| `dev` | Local development (default) | Port 8092, SQL statements are logged. Emails are not sent: verification codes and recovery tokens are written to the console |
| `prod` | Production | Mail credentials come from environment variables |
| `azure` | Azure Container Apps, together with `prod` | Small database pool, proxy headers and graceful shutdown |
| `email` | Real email during local development | Combine with `dev`: selects the configured provider instead of console delivery. Never enabled by automated tests |
| `test` | Automated tests | In-memory H2 database in PostgreSQL compatibility mode, activated by the tests themselves. It needs no environment variables |

For Azure Container Apps resource configuration, runtime secrets, CLI access and deployment
evidence, see the [deployment guide](docs/azure-container-apps.md). This guide describes the
deployed Azure environment. See [Azure email and demo data](docs/azure-email.md) for
provider limits, the retirement notice, synthetic fixtures and remaining validation.

## Running the application

Create an empty PostgreSQL database named `ecomind`, set the environment variables and start the server:

```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/ecomind
export DATABASE_USERNAME=<your-user>
export DATABASE_PASSWORD=<your-password>
export JWT_SECRET=<a-random-secret-of-at-least-32-characters>
export JWT_EXPIRATION_MINUTES=60
export CLOUDINARY_CLOUD_NAME="<your-cloud-name>"
export CLOUDINARY_API_KEY="<your-api-key>"
export CLOUDINARY_API_SECRET="<your-api-secret>"
./mvnw spring-boot:run
```

On Windows (PowerShell):

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/ecomind"
$env:DATABASE_USERNAME = "<your-user>"
$env:DATABASE_PASSWORD = "<your-password>"
$env:JWT_SECRET = "<a-random-secret-of-at-least-32-characters>"
$env:JWT_EXPIRATION_MINUTES = "60"
$env:CULQI_SECRET_KEY = "<your-culqi-secret-key>" # optional
$env:PAYPAL_CLIENT_ID = "<your-paypal-client-id>" # optional
$env:PAYPAL_SECRET = "<your-paypal-secret>" # optional
$env:CLOUDINARY_CLOUD_NAME = "<your-cloud-name>" # optional
$env:CLOUDINARY_API_KEY = "<your-api-key>" # optional
$env:CLOUDINARY_API_SECRET = "<your-api-secret>" # optional
.\mvnw.cmd spring-boot:run
```

The tables are created on the first start. The API is available at `http://localhost:8092`.

To run with the production profile:

```bash
./mvnw clean package
SPRING_PROFILES_ACTIVE=prod java -jar target/ecomind-backend-0.0.1-SNAPSHOT.jar
```

## API documentation

- Swagger UI: `http://localhost:8092/swagger-ui.html`

## Gamification

The first Gamification slice and its integration contract are described in
[docs/gamification.md](docs/gamification.md).
- OpenAPI definition: `http://localhost:8092/v3/api-docs`

The documentation is grouped by bounded context.

## Authentication

Every endpoint requires an access token, except registration, email verification, sign-in,
password recovery and the API documentation. Obtain a token with
`POST /api/v1/authentication/sign-in` and send it in the `Authorization: Bearer <token>` header.

### Password recovery

`POST /api/v1/authentication/password-recovery/request` accepts `{ "email": "user@example.com" }`
and returns `200` with the same message whether or not the account exists.

For the Android app, set the production email destination explicitly:

```bash
export PASSWORD_RECOVERY_URL=ecomind://authentication/reset-password
```

The email service appends the recovery token as the `token` query parameter. Android opens the
reset-password screen from this link both when the app is closed and when it is already running.
The variable remains required in production; it has no default value.

`POST /api/v1/authentication/password-recovery/confirm` accepts `{ "token": "<recovery-token>",
"newPassword": "<new-password>" }` and returns `204` with no response body after changing the
password. Tokens expire after 30 minutes and can only be used once. An invalid, expired or used
token returns `422` with code `PASSWORD_RESET_TOKEN_INVALID`.

### Logout

`POST /api/v1/authentication/logout` requires the current Bearer token and returns `204` with no
response body. Access tokens are stateless and are not revoked by this endpoint. The client must
discard its stored session and return to sign-in.

## Internationalization

Messages are returned in English (`en_US`) by default. Send the `Accept-Language: es-419` header to
receive them in Latin American Spanish.

## Tests

```bash
./mvnw clean verify
```

This runs the unit tests and the acceptance tests written with Cucumber. Feature files are located in
`src/test/resources/features` and their step definitions in `src/test/java/pe/greenminds/ecomind/bdd`.


## Continuous integration and delivery

The [backend workflow](.github/workflows/backend.yml) verifies pull requests with H2 and PostgreSQL, enforces coverage, scans the Azure image and retains reports. Merges into `develop` deploy the same image to Azure by digest through OIDC, verify the ready revision and public API, and restore the previous image if rollout checks fail when safe. See [CI/CD configuration](docs/ci-cd.md) for GitFlow, configuration, checks and recovery.

## Verifying real IAM email

For local development with real delivery, use `SPRING_PROFILES_ACTIVE=dev,email` and configure
`RESEND_API_KEY` and `RESEND_FROM_EMAIL` in the private environment of the backend process.
Keep the existing database and JWT environment settings. Production must use `prod`.
The `test` profile never selects the real mail adapter.

The sender must be permitted by the Resend account. The Resend sandbox sender only delivers
testing mail to the account owner; arbitrary recipients require a verified sending domain.
Do not create a new account or change DNS as part of a code-only test.

The request sends both HTML and plain text, in the request language. Recovery includes the
Android link, its 30-minute expiry, one-use guidance and an ignore-if-not-requested notice.
Open it on a device with EcoMind installed. A new link replaces any configured `token` query
parameter and encodes the issued token. Provider connection and response waits are bounded;
provider rejection raises a delivery error. Password recovery records a warning without recipient
or token data and keeps the same generic acknowledgement for registered and unknown accounts.
The acknowledgement confirms receipt of the request, not successful email delivery.

Before claiming real delivery, the service administrator must verify the deployed profile,
mail settings, public API and sender, then request recovery for a controlled registered inbox.
Verify reception and the app link, reset the password, sign in with the new password and reject
the used link. A mocked provider response or console token is not evidence of inbox delivery.

References: [Resend send API](https://resend.com/docs/api-reference/emails/send-email) and
[Resend errors and testing-recipient restrictions](https://resend.com/docs/api-reference/errors).
