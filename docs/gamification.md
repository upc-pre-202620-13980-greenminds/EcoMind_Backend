# Gamification implementation status

This bounded context follows section 2.6.6 of the current EcoMind report. The implementation owns user ecopoints, experience, daily streaks, family ecopoints and the history
of validated quest and family plan rewards.

## Current contract

- Quests must validate a completion before calling `RewardCommandService.handle` with a stable
  `sourceExecutionId`, the beneficiary's IAM account ID, an activity date and the configured base
  reward. There is intentionally no public endpoint for granting points.
- `RewardTransaction` is unique by source type, canonical execution and beneficiary. Repeated
  delivery returns the original transaction. The reward and progress update commit together; a
  user progress row is locked to serialize concurrent deliveries.
- Only the first eligible daily quest increases the streak. Other valid completions still grant
  their ecopoints and experience. A late event does not rewind the latest activity date.
- A JWT holder can read only their own data through `GET /api/v1/gamification/me/progress` and
  `GET /api/v1/gamification/me/rewards`. The latter returns at most 100 recent transactions.
- Quest rewards with gems are rejected until Monetization can receive a durable, idempotent credit
  request; no gem balance is changed by this slice.

The report's proposed SQL uses UUIDs for external user references. This backend already uses
numeric IAM account IDs, so Gamification stores those as `BIGINT` without a cross-context foreign
key. Its physical naming strategy pluralizes `user_progresses`. These adaptations should be
reflected in the report's physical database diagram when the schema is finalized.

## Family scores and rewards

- `GET /api/v1/gamification/families/{familyId}/score` returns the family's ecopoints, initially zero.
- `GET /api/v1/gamification/families/{familyId}/rewards` returns its 100 most recent grants.
- Both queries require a JWT and current family membership. Removed members lose access. A missing
  family and a family belonging to somebody else both produce 403, without disclosing its existence.
- The trusted `FamilyRewardCommandService` accepts a validated plan execution and its configured
  additional ecopoints. It does not sum member rewards or grant XP/gems to families. Zero additional
  ecopoints records the completion without increasing the score; it does not grant an achievement.
- The family grant and score commit atomically, with a per-family lock and the shared reward origin
  uniqueness constraint. Repeated executions return the original grant, including concurrent delivery.
- Users exposes membership through `UsersContextFacade`; Gamification's ACL maps this public contract
  without reading Users tables or importing its domain classes. No public endpoint can grant points.

## Integration work still needed

1. Quests completion events and its canonical execution/reward contract. Until that exists, the
   trusted command service is callable in code but no production completion grants a reward.
2. Users currently stores `ecopoints` and `streak` in its profile and lets clients replace them.
   Change the profile to read Gamification's values and remove that client-writable source.
3. Monetization outbox and acknowledgements for gems, active XP multipliers and streak protectors.
4. Configurable achievements, Community awards/sharing, and authorized
   ranking queries. These require the corresponding Quests, Users, Community and Monetization
   contracts; do not invent reward thresholds or family membership.

Run the JUnit suite with `./mvnw test` (or `mvn test` where Maven is installed). The new tests
cover daily streak rules, idempotent and concurrent grants, persistence, JWT scope and unauthenticated
requests using H2 in MySQL mode. Family tests also verify removed-member access, zero initial score,
score overflow rollback and separation from individual rewards. Native MySQL execution remains unverified.
