# Automated backend tests

`bash ./mvnw clean verify` runs JUnit unit/integration tests and the Cucumber acceptance suite. The same command runs in GitHub Actions against H2 and PostgreSQL 17.

| Context | Behaviors exercised | Test location |
|---|---|---|
| IAM | Registration, email verification, login, password recovery and logout | `src/test/java/pe/greenminds/ecomind/iam`, `src/test/resources/features/iam` |
| Users | Profiles, family membership and friend requests | `src/test/java/pe/greenminds/ecomind/users`, `src/test/resources/features/users` |
| Quests | Draft updates and versioning, publication/archive, ordered activities, daily assignments and expiry, minigame score thresholds, completion and cancellation, assignment ownership, atomic family plan completion, collaborative invitations/acceptance/decline/leave/removal and shared progress | `src/test/java/pe/greenminds/ecomind/quests`, `src/test/resources/features/quests` |
| Community | Membership, community capacity, parent/admin permissions, publications and reactions, event registration/cancellation/deletion permissions, goal completion | `src/test/java/pe/greenminds/ecomind/community`, `src/test/resources/features/community` |
| Gamification | Ecopoints, daily streaks, idempotent rewards, achievements, rankings, family scores, outbox retries and concurrent deliveries | `src/test/java/pe/greenminds/ecomind/gamification` |
| Monetization | Catalog, wallet, inventory, purchase idempotency, payment adapters, insufficient funds and request validation | `src/test/java/pe/greenminds/ecomind/monetization`, `src/test/resources/features/monetization` |
| Shared | Localized error responses and authentication required by protected endpoints | `src/test/java/pe/greenminds/ecomind/shared` |

## Acceptance traceability

| Feature | User stories |
|---|---|
| `iam/hu056_user_registration.feature` | HU-056 |
| `iam/hu057_sign_in.feature` | HU-057 |
| `iam/hu058_password_recovery.feature` | HU-058 |
| `iam/hu059_logout.feature` | HU-059 |
| `users/hu019_family_group.feature` | HU-019 |
| `users/hu039_friend_requests.feature` | HU-039 |
| `quests/guided_quests.feature` | HU-001, HU-004, HU-046 |
| `quests/collaborative_lifecycle.feature` | HU-001, HU-004 |
| `community/community_participation.feature` | HU-014, HU-033, HU-034, HU-037, HU-060, HU-061, HU-062, HU-063, HU-064 |
| `monetization/store_and_wallet.feature` | HU-029, HU-035, HU-067 |

Step definitions call the REST endpoints through `MockMvc`, using the actual Spring security chain, application services and JPA adapters. Acceptance users are registered and authenticated through IAM. Scenario hooks clear their Community and Quests data; IAM/Users hooks clear accounts and profiles. Payment-provider integration tests use local test adapters. No production accounts or credentials are used.

## Results

- `target/surefire-reports`: JUnit execution results, failures and stack traces.
- `target/cucumber/report.html`: executable Gherkin scenarios and step results.
- `target/cucumber/report.json` and `report.xml`: machine-readable acceptance results.
- `target/site/jacoco/index.html`: instruction, branch and line coverage, by package and class.

A passing suite describes the executed cases; the JaCoCo report supplies the measured coverage. [CI/CD configuration](ci-cd.md) documents the workflow and deployment gate.

## Coverage gate

JaCoCo checks the complete backend bundle during `verify`. At least 85% of lines and 65% of branches must be covered. Falling below either threshold fails local verification and both CI database jobs.
