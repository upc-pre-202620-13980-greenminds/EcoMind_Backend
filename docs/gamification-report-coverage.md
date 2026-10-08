# Report → Gamification backend coverage

Reviewed authority: report `origin/develop` **0d0783341b9f0b77883c65068d0af4809a74f5b9**:
section 2.6.6, associated class/database sources, HU-040/HU-041, TS-006/TS-007.
Legacy `GreeenMinds/EcoMind_backend` and `ICEQ2026/coldtrace-platform` supply Java style references;
they do not override current report behavior. Domain objects remain persistence-independent;
commands/queries are records, application services coordinate transactions, JPA adapters implement
repository ports, and REST resources use dedicated static assemblers and shared Result/error responses.

`Validated` below means covered by the named executable tests. It does not mean Android or absent
external suppliers have been implemented. Database/JUnit run results and UI media are local evidence.

| Report requirement | Backend evidence | Executable verification / boundary |
|---|---|---|
| 6 source types; canonical execution + beneficiary uniqueness | `RewardCommandServiceImpl`, `FamilyRewardCommandServiceImpl`, reward origin constraint | `RewardCommandServiceIntegrationTests`, `FamilyGamificationTests`, report integration suite |
| Atomic completion/reward/score/achievement/outbox | Synchronous Quests consumers; transactional command services and outbox publisher | rollback and duplicate/concurrent delivery tests |
| Separate user XP/ecopoints and family ecopoints | `UserProgress`, `FamilyScore`; own-progress/score foreign keys | individual/collaborative/family tests; family XP/gems prohibited by DB |
| `RewardSource`, `RewardBeneficiary`, `Reward` | Typed value objects and canonical transaction/beneficiary views; family adapter shares reward table | all grant tests |
| Configured active XP multiplier only | `RewardCalculationService`; persisted multiplier ID/effective factor | multiplier boundaries, floor and audit round-trip test |
| Strict minigame decrease in preceding 3 hours (HU-041) | `MinigameRepetitionPolicy`, validated attempts facade, own completion ledger | approved 100/80/50/20/0, reset, retry, independent/concurrent user tests |
| First eligible daily quest increments once | `Streak`, `StreakService`, `UserProgressCommandServiceImpl` | repeated day, non-daily, protected date and next-day tests |
| Closed-day lifecycle and restart recovery | `DailyStreakClosureService` (report's `DailyStreakLifecycleService`) | closure idempotence and real inventory result tests |
| Protector pending/protected/unavailable; technical failure pending | Correlated requests/results; own-progress FK; version and resolved timestamp | real inventory debit, no inventory, mismatch/conflict and pending retry tests |
| Family plan recognition and optional additional configured reward | Family reward + milestone; no sum of individual quests | family isolation, duplicate and completed-plan criteria |
| Individual/family/community catalog and criteria | `Achievement`, `AchievementCriterion`, `AchievementEvaluationService` | configured thresholds; incompatible metric/scope rejected |
| Individual badges for eligible community participants (HU-040) | `CommunityProgressConsumer`, milestone repository and community locks | collective and eligible individual awards, no fabricated reward; concurrent threshold test |
| One scope-compatible beneficiary and one award | Award checks, own catalog/progress/score FKs and unique constraints | repeated/concurrent awards; DB integrity tests |
| Optional individual cosmetic | `AchievementUnlockedEventHandler` → durable outbox → real Monetization | actual cosmetic grant once; no family/community cosmetic |
| Notice differs from voluntary post | Separate ACHIEVEMENT_NOTICE and SHARE_ACHIEVEMENT messages | publication request/status and acknowledgement tests; actual Community supplier pending |
| Stable share request and ownership/membership/permission checks | Share command/resource assembler, requester from JWT, share repository | foreign owner, membership, permission, reused ID, concurrent retry and status authorization tests |
| Pending until exact persisted publication confirmation | `AchievementPublicationConsumer`, award FK, unique publication, confirmed timestamp | repeated/mismatched/unknown/delayed confirmation tests |
| Read-only progress, family score and period history queries | Query services; inclusive start/exclusive end; owner/member checks | REST/JWT history and family access tests |
| Catalog/scope and scoped award queries | Achievement query service; no grants on read | `AchievementTests` and report suite; community membership contract mocked explicitly |
| LOCAL/GLOBAL/FRIENDS/FAMILIES | Public Users/Community directory adapters + ranking read repository | `RankingTests`, LOCAL membership test; real LOCAL supplier pending |
| TS-006/TS-007 mock route compatibility | `LegacyGamificationController`; Users profile reads Gamification | alias authorization and Users score response test |
| Android computes weekly positions | Participants + dated ecopoint transactions, stable pagination with `hasNext` | period/pagination/read-only tests; Android implementation outside backend scope |
| Quests 4 finalization consumers | Proposed `QuestCompletionConsumer`; actual `PublishedQuestCompletionConsumer` | actual event and validated minigame history tests; base XP configuration required |
| Community goal/event and publication consumers | Real Gamification listeners for public Community contracts | integration tests with explicit Community mock; actual producer pending |
| All named internal facts and outgoing integration messages | `domain.model.events`, application event handlers and public integration records | transactional rollback and each delivery/ack path |
| Durable outgoing communication and receiver deduplication | `SpringGamificationEventPublisher`, `GamificationOutboxPublisher`, delivery service | real gem/cosmetic/protector idempotence; failure/retry and delayed confirmation tests |
| Optimistic versions and DB business constraints | JPA mappings + additive PostgreSQL migration | native PostgreSQL and orphan/status/nonnegative constraint tests |

## Explicit implementation adaptations

- Report identifiers are proposed UUIDs. The published backend uses numeric external account/family/
  quest IDs. These remain canonical; no unrelated UUID mapping is fabricated for accounts/families.
  Numeric progress/score primary keys have local reward/award FK references, rather than separate
  UUID surrogate progress IDs. External references never join another bounded context's tables.
- `RewardTransaction` is the user grant aggregate; the family command adapter uses the immutable
  `FamilyRewardTransaction` and the same polymorphic persisted reward ledger. Both preserve the
  report's source/beneficiary uniqueness and family-only ecopoint rules.
- The report's proposed SQL is labelled MySQL 8. Actual storage is PostgreSQL with `ON CONFLICT`.
  Physical pluralized table names and typed outbox columns differ from the proposed SQL/JSON design.
  Historical records have factor=1 and unknown multiplier ID after migration; do not claim that an
  earlier unrecorded multiplier can be reconstructed.
- Combined REST/controller and event-consumer classes implement some report component responsibilities;
  their names are not a literal one-class-per-diagram translation. Dedicated services, ports, commands,
  query records, domain services and assemblers preserve the report's layer boundaries.
- `RankingPage`/`RankingEntry` are paged read models; no duplicated persistent ranking-position table.
  Users responses now read Gamification score/streak; old Users writable counters are not ranking truth.
  Removal of those deprecated profile-input fields and wallet-field ownership remains Users work.

## Decisions and remaining integration inputs

1. **Approved:** minigame curve is 100%,80%,50%,20%,then 0, configurable, within three hours. The old
   backend's second full reward was intentionally changed to satisfy current HU-041.
2. **Needs configured values:** real Quests does not expose base XP. `ConfigureQuestExperienceCommand`
   supplies trusted per-version XP; missing configuration explicitly fails. There is no approved
   XP=ecopoints conversion and no invented default. Configure the production quest versions before
   allowing their completion. The local QA fixture's values are synthetic evidence only.
3. **External supplier missing:** Community must implement the public membership/publication/notice
   facade and emit goal/event/publication confirmations. Those paths are contract-tested, not proven
   against a real Community backend. Absence is explicit (503 or durable pending outbox).
4. **Separate deliverables:** Android gateways, repositories, ViewModels, retained share drafts,
   weekly sorting and dialogs in table 102 are mobile work; Community owns published-post filters,
   feed and animation. This backend work does not claim those deliverables complete.
5. **Report consistency:** update proposed physical DB SQL/diagram to PostgreSQL/numeric published IDs
   at team integration. Existing report working-tree edits are preserved; no PDF was regenerated.
