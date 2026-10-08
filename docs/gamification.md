# Gamification backend

Authority: `GreenMinds_Report`, `origin/develop` at
`0d0783341b9f0b77883c65068d0af4809a74f5b9`, section 2.6.6, HU-040/HU-041,
TS-006/TS-007 and the Gamification class/database diagram sources.
See [report coverage](gamification-report-coverage.md) and [integration contracts](integration-contracts.md).

## Rewards and progress

Validated individual quests, minigames, collaborative sessions, family plans, community goals and
community events have separate canonical execution identities. A reward is unique by source,
execution and beneficiary, independently of the message UUID. Reward, score, eligible daily activity,
achievements and outgoing messages commit in one transaction. Per-beneficiary locks serialize writes;
progress, scores, publication requests and protection requests also have optimistic versions.

Only the first valid **daily quest** on a calendar day increases the streak. Other completions keep
their rewards. The activity zone defaults to `America/Lima`. Protected dates are separate from actual
activity. Daily closure requests protection for a missing closed day; it does not debit inventory.
Monetization confirms `PROTECTED` or `UNAVAILABLE`. Technical failures stay pending and retryable.
A later daily completion waits for earlier pending protection instead of prematurely resetting a streak.

XP is the ecopoints score and uses the active Monetization multiplier, with inclusive start and
exclusive expiry. Gems are not affected by that multiplier. The reward audit stores the multiplier
identity, effective XP factor and minigame repetition factor. Integer results are rounded down; arithmetic overflow rolls back the grant.

**Approved minigame policy:** 100%, 80%, 50%, 20%, then 0 in the preceding three hours, independently
per user and minigame. Zero-valued validated attempts still count. Set
`MINIGAME_REPETITION_FACTORS` to override the strictly decreasing curve. This deliberately differs
from the legacy `100%,100%,80%,50%,20%` curve because HU-041 requires each repetition to decrease.

Family plans receive only a configured **additional** ecopoint reward. Their member quests are not
summed or rewarded again. A zero additional bonus still records a validated completion and evaluates
configured completed-plan achievements. Families receive the ecopoints score and no gems.

## Achievements and publication

The trusted catalog service configures definitions; there is no public mobile grant/catalog-write route
and no invented production threshold. Individual criteria: ecopoints (XP), longest streak or completed
community goals. Family criteria: ecopoints or completed family plans. Collective community criteria:
completed community goals. Only individual awards can request an optional cosmetic.

Awards have one scope-compatible beneficiary and are unique per definition and beneficiary.
Community completion records a collective milestone and the eligible participants' milestones;
recognition does not invent points when no prize was configured. Reading never grants an award.

Sharing requires an existing individual award owned by the JWT subject, destination membership and
permission to publish. Use the same client `requestId` on retries. A changed selection with that ID
returns 409; another requester is denied. The request and outgoing message commit atomically.
`PENDING` changes to `PUBLISHED` only after a matching Community confirmation. Duplicate matching
confirmations preserve the publication and confirmation date; conflicting confirmations fail.
Only the requester can query status. Community owns posts/feed, including shared individual awards.
Collective award queries do not substitute for that feed.

## Authenticated REST routes

| Method | Route | Access / result |
|---|---|---|
| GET | `/api/v1/gamification/me/progress` | JWT subject's ecopoints (XP), streak and activity/protection dates |
| GET | `/api/v1/gamification/me/rewards` | Subject's latest 100 reward transactions |
| GET | `/api/v1/gamification/rewards` | Paged USER/FAMILY history; `from` inclusive, `to` exclusive; owner/membership checks |
| GET | `/api/v1/gamification/families/{familyId}/score` | Current family members only |
| GET | `/api/v1/gamification/families/{familyId}/rewards` | Current members; latest 100 family grants |
| GET | `/api/v1/gamification/achievements` | Catalog; optional scope; page/size |
| GET | `/api/v1/gamification/achievements/{achievementId}` | Catalog detail or 404 |
| GET | `/api/v1/gamification/me/achievements` | Subject's individual awards |
| GET | `/api/v1/gamification/families/{familyId}/achievements` | Current family members only |
| GET | `/api/v1/gamification/communities/{communityId}/achievements` | Collective awards; community members only |
| POST | `/api/v1/gamification/achievement-shares` | `{requestId,awardId,communityId}`; requester from JWT; 202 |
| GET | `/api/v1/gamification/achievement-shares/{requestId}` | Original requester only |
| GET | `/api/v1/gamification/rankings/types` | LOCAL, GLOBAL, FRIENDS, FAMILIES |
| GET | `/api/v1/gamification/rankings/{type}/participants` | Authorized directory and recorded ecopoints |
| GET | `/api/v1/gamification/rankings/{type}/transactions` | Authorized ecopoint transactions within `from`/`to` |

TS-006/TS-007 compatibility aliases: `/api/v1/achievement`, `/api/v1/user_achievement?user_id=…`,
`/api/v1/community_achievement?community_id=…`, `/api/v1/ranking`, `/api/v1/ecopoint_transaction`.
`/api/v1/user` remains in Users and its ecopoints/streak response reads Gamification.
The requesting user cannot select another user's awards/history. Removed family members lose access.
Pages start at zero; size is 1–100. Ranking/history pages include `hasNext`, with stable date/ID ordering.

GLOBAL uses Users profiles; FRIENDS includes self and accepted friends; FAMILIES uses family scores;
LOCAL requires Community's membership/directory supplier. Android aggregates all transaction pages
and calculates weekly positions (TS-007). These read-only routes never award points or expose email,
wallet balances or private relationship lists.

## Durable outgoing delivery

`SpringGamificationEventPublisher` records messages in `gamification_outbox` in the business transaction.
The scheduled publisher delivers gems, cosmetics, achievement notices, share requests and protection
requests with row locks, durable retries and capped exponential backoff. Receivers deduplicate by reward,
award or request. Share/protection delivery remains pending until correlated business acknowledgement;
an acknowledgement received later completes delivery without resending the request.

Real Monetization from `feature/store` is integrated. Community has public ports/event contracts but no
published supplier in the branches integrated here. Community-dependent REST reads/shares return 503
while that supplier is absent; notices remain in the outbox. JUnit uses an explicit Community mock to
validate its contract, permissions, retry and confirmation behavior; it is not a production stub.

Real Quests emits four completion events and has a public validated minigame-history supplier.
XP and ecopoints identify the same score. Quests provides the base ecopoints and gems; completion
requires no additional XP configuration. An active XP multiplier affects ecopoints only, leaving gems
unchanged by that multiplier. Existing repetition reduction still applies to the configured reward.
Historical XP columns remain for schema compatibility and are ignored when loading balances; their
values are not added to ecopoints. Existing EXPERIENCE achievement definitions use ecopoints.
Actual family events have no additional bonus field, so their adapter records completion/recognition
with no extra points. The proposed reward-complete event supports a configured additional bonus.

## PostgreSQL and physical adaptations

Numeric published Users/Quests IDs are retained, rather than converting account/family IDs to invented
UUIDs. Gamification's user/family canonical identity is also its local progress/score primary key.
Local reward/award relations use foreign keys to Gamification progress, score and catalog records;
external user/family/community/cosmetic/multiplier references have no cross-context FK. JPA entities
are infrastructure adapters; the domain is persistence-independent. The outbox uses typed columns.
These are explicit adaptations of the report's proposed MySQL/UUID SQL; update the report's physical
schema when the team finalizes it. Do not describe that proposed SQL as the actual PostgreSQL schema.

For an existing database, apply [the additive migration](migrations/20261007-gamification-postgresql.sql)
after Hibernate creates the new tables. It backfills local references/factors and enforces constraints;
it does not erase data. Fresh test databases are created from JPA mappings.

## JUnit validation

`JAVA_HOME=/opt/homebrew/opt/openjdk@21 ./mvnw test` runs all JUnit and acceptance suites on H2 in
PostgreSQL mode. For native persistence validation use a **dedicated disposable** PostgreSQL database:

```bash
createdb ecomind_gamification_test
TEST_DATABASE_URL=jdbc:postgresql://localhost:5432/ecomind_gamification_test \
TEST_DATABASE_DRIVER=org.postgresql.Driver \
TEST_DATABASE_USERNAME=your_local_user \
TEST_DATABASE_PASSWORD='' \
./mvnw test -Dtest=AchievementTests,FamilyGamificationTests,QuestIntegrationTests,RankingTests,RewardCommandServiceIntegrationTests,GamificationReportIntegrationTests
```

Tests use `create-drop`; never target the application database. Coverage includes all six reward
sources, canonical retries/concurrency, multipliers, repetition/reset, actual gem/cosmetic/protector
services, atomic rollback, collective/individual achievement criteria, sharing/correlation, missing
suppliers/configuration, closure recovery, authorization and read-only ranking periods. QA media and
synthetic fixtures stay local outside repositories under `~/Downloads/EcoMind-qa-evidence/`.

## Community integration (2026-10-08)

The branch now imports Leo's `feature/community` implementation. Gamification uses its numeric
community ids and real membership repositories through `CommunityContextFacade`; LOCAL participants
come from those memberships and public Users names. Share input `communityId` and confirmed
`publicationId` are positive numbers. Request and award ids remain UUIDs.

A voluntary share is delivered through the existing durable outbox. Community stores an achievement
post and confirms it atomically with Gamification's share state. Notice delivery is deduplicated and
never creates a post. Authorized members can read/filter achievement publications at
`GET /api/v1/Community/Communities/{communityId}/AchievementPosts`.

Community goal/event completion remains a contract dependency until those producers are implemented.
Production Quests XP remains an explicit configuration decision; no default XP values were added.
