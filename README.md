# EcoMind Backend

RESTful web services of the EcoMind platform, built with Spring Boot and Java.

## Requirements

- Java 21 (JDK)
- PostgreSQL
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

Credentials are never stored in the repository. Set these variables before running the application.

| Variable | Profile | Description |
|---|---|---|
| `DATABASE_URL` | dev (optional), prod | JDBC URL of the PostgreSQL database. In `dev` it defaults to `jdbc:postgresql://localhost:5432/ecomind` |
| `DATABASE_USERNAME` | dev, prod | Database user |
| `DATABASE_PASSWORD` | dev, prod | Database password |
| `JWT_SECRET` | dev, prod | Secret used to sign access tokens. At least 32 characters |
| `JWT_EXPIRATION_MINUTES` | dev, prod | Minutes an access token is valid |
| `RESEND_API_KEY` | prod | API key of the Resend email service |
| `RESEND_FROM_EMAIL` | prod | Sender address of the emails, verified in Resend |
| `PASSWORD_RECOVERY_URL` | prod | URL of the screen that sets a new password. The recovery token is appended as the `token` query parameter |
| `PORT` | prod | HTTP port the application listens on |
| `SPRING_PROFILES_ACTIVE` | prod | Must be `prod` in the production environment |

## Profiles

| Profile | Use | Notes |
|---|---|---|
| `dev` | Local development (default) | Port 8092, SQL statements are logged. Emails are not sent: verification codes and recovery tokens are written to the console |
| `prod` | Production | Every setting comes from environment variables |
| `test` | Automated tests | In-memory H2 database, activated by the tests themselves |

## Running the application

Create an empty PostgreSQL database named `ecomind`, set the environment variables and start the server:

```bash
export DATABASE_USERNAME=<your-user>
export DATABASE_PASSWORD=<your-password>
export JWT_SECRET=<a-random-secret-of-at-least-32-characters>
export JWT_EXPIRATION_MINUTES=60
./mvnw spring-boot:run
```

On Windows (PowerShell):

```powershell
$env:DATABASE_USERNAME = "<your-user>"
$env:DATABASE_PASSWORD = "<your-password>"
$env:JWT_SECRET = "<a-random-secret-of-at-least-32-characters>"
$env:JWT_EXPIRATION_MINUTES = "60"
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:8092`.

To run with the production profile:

```bash
./mvnw clean package
SPRING_PROFILES_ACTIVE=prod java -jar target/ecomind-backend-0.0.1-SNAPSHOT.jar
```

## API documentation

- Swagger UI: `http://localhost:8092/swagger-ui.html`
- OpenAPI definition: `http://localhost:8092/v3/api-docs`

The documentation is grouped by bounded context.

## Authentication

Every endpoint requires an access token, except registration, email verification, sign-in,
password recovery and the API documentation. Obtain a token with
`POST /api/v1/authentication/sign-in` and send it in the `Authorization: Bearer <token>` header.

## Internationalization

Messages are returned in English (`en_US`) by default. Send the `Accept-Language: es-419` header to
receive them in Latin American Spanish.

## Tests

```bash
./mvnw clean verify
```

This runs the unit tests and the acceptance tests written with Cucumber. Feature files are located in
`src/test/resources/features` and their step definitions in `src/test/java/pe/greenminds/ecomind/bdd`.
