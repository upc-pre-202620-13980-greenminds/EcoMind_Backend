# EcoMind Backend

RESTful web services of the EcoMind platform, built with Spring Boot and Java.

## Requirements

- Java 21 (JDK)
- MySQL 8
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
| `DATABASE_URL` | dev (optional), prod | JDBC URL of the MySQL schema. In `dev` it defaults to `jdbc:mysql://localhost:3306/ecomind` |
| `DATABASE_USERNAME` | dev, prod | Database user |
| `DATABASE_PASSWORD` | dev, prod | Database password |
| `PORT` | prod | HTTP port the application listens on |
| `SPRING_PROFILES_ACTIVE` | prod | Must be `prod` in the production environment |

## Profiles

| Profile | Use | Notes |
|---|---|---|
| `dev` | Local development (default) | Port 8092, SQL statements are logged |
| `prod` | Production | Every setting comes from environment variables |
| `test` | Automated tests | In-memory H2 database, activated by the tests themselves |

## Running the application

Create an empty MySQL schema named `ecomind`, set the environment variables and start the server:

```bash
export DATABASE_USERNAME=<your-user>
export DATABASE_PASSWORD=<your-password>
./mvnw spring-boot:run
```

On Windows (PowerShell):

```powershell
$env:DATABASE_USERNAME = "<your-user>"
$env:DATABASE_PASSWORD = "<your-password>"
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

## Internationalization

Messages are returned in English (`en_US`) by default. Send the `Accept-Language: es-419` header to
receive them in Latin American Spanish.

## Tests

```bash
./mvnw clean verify
```

This runs the unit tests and the acceptance tests written with Cucumber. Feature files are located in
`src/test/resources/features` and their step definitions in `src/test/java/pe/greenminds/ecomind/bdd`.
