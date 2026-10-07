# Gamification implementation status

This bounded context follows section 2.6.6 of the current EcoMind report. The first executable
slice owns user ecopoints, experience, daily streaks and the history of validated quest rewards.

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

## Integration work still needed

1. Quests completion events and its canonical execution/reward contract. Until that exists, the
   trusted command service is callable in code but no production completion grants a reward.
2. Users currently stores `ecopoints` and `streak` in its profile and lets clients replace them.
   Change the profile to read Gamification's values and remove that client-writable source.
3. Monetization outbox and acknowledgements for gems, active XP multipliers and streak protectors.
4. Family scores and rewards, configurable achievements, Community awards/sharing, and authorized
   ranking queries. These require the corresponding Quests, Users, Community and Monetization
   contracts; do not invent reward thresholds or family membership.

Run the JUnit suite with `./mvnw test` (or `mvn test` where Maven is installed). The new tests
cover daily streak rules, idempotent and concurrent grants, persistence, JWT scope and unauthenticated
requests using the existing H2 test profile.
