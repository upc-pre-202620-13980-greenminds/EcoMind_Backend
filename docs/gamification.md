# Gamification implementation status

This bounded context follows section 2.6.6 of the current EcoMind report. The implementation owns user ecopoints, experience, daily streaks, family ecopoints and the history
of validated quest and family plan rewards, plus configurable individual and family achievements.

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

## Achievement catalog and awards

Authenticated endpoints:

- `GET /api/v1/gamification/achievements?scope=INDIVIDUAL&page=0&size=20` lists catalog definitions.
  The scope filter is optional; inactive definitions remain visible for historical awards.
- `GET /api/v1/gamification/achievements/{achievementId}` returns a definition or 404.
- `GET /api/v1/gamification/me/achievements` lists awards owned by the JWT subject.
- `GET /api/v1/gamification/families/{familyId}/achievements` requires current family membership.

Lists support zero-based `page` and `size` from 1 to 100. Invalid pagination/scope produces 400.
Catalog codes are unique and definitions immutable through the current application port.
`AchievementCommandService.register` is a trusted configuration operation, with no public mobile
write endpoint. No production thresholds or example achievements are seeded. The configured
catalog must be supplied before users can earn these achievements.

Active individual definitions can use `ECOPOINTS`, `EXPERIENCE` or `LONGEST_STREAK`; families use
`ECOPOINTS`. Targets must be positive. Each newly recorded quest/family reward evaluates the matching
scope using persisted progress in the same transaction. The database guarantees a single award per
achievement, scope and beneficiary; further rewards and retries preserve its original source and date.
Catalog registration itself does not retroactively evaluate users; new definitions are evaluated on
subsequent new rewards. A lookup does not grant anything.

Community criteria, cosmetic prizes, publication requests and integration events/outbox delivery are
not yet implemented. Unsupported community definitions are rejected instead of being evaluated with
individual or family metrics. Achievement sharing and Community notifications remain separate future flows.

## Ranking data (TS-007)

All routes require a JWT:

- `GET /api/v1/gamification/rankings/types` returns `GLOBAL`, `FRIENDS`, `FAMILIES`.
- `GET /api/v1/gamification/rankings/{type}/participants?page=0&size=20` returns participant IDs,
  display names and accumulated ecopoints owned by Gamification, including zero-progress participants.
  Participants are ordered by identity; this is not a score-sorted ranking or a weekly position.
- `GET /api/v1/gamification/rankings/{type}/transactions?from=2026-10-05T05:00:00Z&to=2026-10-12T05:00:00Z&page=0&size=100`
  returns ecopoint transactions for the permitted participants. `from` is inclusive and `to` exclusive.
  Clients supply the UTC boundaries appropriate to their calendar, aggregate all pages, and calculate
  positions. This implements the responsibility assigned to Android by TS-007.

Responses contain `items`, `page`, `size`, `hasNext`. Page numbering starts at zero; size is 1–100.
Transaction ordering is stable by date and ID. `hasNext` prevents silently truncating weeks with more
than 100 grants. Pagination reflects current data; there is no cross-request snapshot token.

`GLOBAL` uses the Users profile directory. `FRIENDS` includes the JWT holder and accepted friends in
both relationship directions, excluding pending/rejected/deleted relationships. `FAMILIES` uses family
names and only rewards whose beneficiary type is FAMILY. No email, wallet, XP or private family membership
is returned. All queries are read-only and ignore editable progress fields in legacy Users profiles.
The public Users ACL supplies directory data; Gamification does not join Users tables.

`LOCAL` is not advertised and is rejected until Community provides its membership contract. The current
Users directory contract returns all profiles/families before filtering or paging; large deployments
will need a paginated directory API. Routes replace the mock API route shapes in TS-007, while preserving
its split between participant data, dated transactions and client-side weekly position calculation.

## Integration work still needed

1. Quests completion events and its canonical execution/reward contract. Until that exists, the
   trusted command service is callable in code but no production completion grants a reward.
2. Users currently stores `ecopoints` and `streak` in its profile and lets clients replace them.
   Change the profile to read Gamification's values and remove that client-writable source.
3. Monetization outbox and acknowledgements for gems, active XP multipliers and streak protectors.
4. Community awards/sharing, cosmetic achievement rewards, and local
   ranking membership from Community. These require the corresponding Quests, Users, Community and Monetization
   contracts; do not invent reward thresholds or family membership.

Run the JUnit suite with `./mvnw test` (or `mvn test` where Maven is installed). The new tests
cover daily streak rules, idempotent and concurrent grants, persistence, JWT scope and unauthenticated
requests using H2 in MySQL mode. Family tests also verify removed-member access, zero initial score,
score overflow rollback and separation from individual rewards. Native MySQL execution remains unverified.
