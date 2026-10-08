# Gamification integration contracts

The public boundaries follow report section 2.6.6. See [Gamification behavior](gamification.md) and
[requirement coverage](gamification-report-coverage.md). Contracts represent implemented adapters
or explicit external dependencies; interface existence alone is not evidence of a working supplier.

| Owner | Contract | State |
|---|---|---|
| Quests | Actual four `quests.interfaces.events` completion events | Consumed by `PublishedQuestCompletionConsumer` in the source transaction |
| Quests | Proposed reward-complete `interfaces.acl.events` | Consumed by `QuestCompletionConsumer`; real producer does not emit these proposed messages |
| Quests | `PublishedQuestsContextFacade` | Real public supplier for completed validated minigame attempts, backed by Quests repositories |
| Quests | Proposed `QuestsContextFacade` | Separate UUID contract; no supplier; fails explicitly when called |
| Monetization | `MonetizationContextFacade` | Real supplier for active XP multiplier, gem credit, cosmetic grant and protector consumption from merged store branch |
| Monetization | Protected/unavailable events | Real correlated results consumed by Gamification; technical failure is not missing inventory |
| Community | `CommunityContextFacade` | Public port; supplier absent from published branches integrated here |
| Community | Goal/event completion, publication confirmation events | Gamification consumers implemented; actual Community producer pending |
| Gamification | `GamificationContextFacade` | Read-only progress snapshot used by Users/Monetization |
| Gamification | Domain events + `SpringGamificationEventPublisher` | Working synchronous internal delivery + transactional outgoing outbox |
| Gamification | `GamificationOutboxPublisher` | Durable retry/acknowledgement delivery to public supplier ports |

## Canonical identities and base amounts

Published Quests IDs are numeric. The adapter derives a stable internal UUID from execution type and
actual execution ID, rather than deduplicating on a message UUID. It maps the event's amounts and
calendar to Gamification value objects. The actual event's public quest-type value determines daily
eligibility; no Quests domain/repository is accessed by Gamification. Only Quests' own public facade
implementation reads its validated attempt repository.

**Unresolved configuration:** published Quests has no base XP. Trusted per-version XP configuration is
required; missing values raise a dependency error and roll back the completion. XP is not silently set
to zero or equated to ecopoints. Actual family events do not configure an additional reward; the adapter
records the validated milestone with zero additional points. It does not sum member grants. The proposed
reward-complete family event can supply a configured bonus. No public mobile API can forge these inputs.

## Transactions and retries

Actual Quests publishes synchronously in its transaction. A Gamification listener failure rolls back
completion, rewards, score, achievements and outbox together. Outgoing gem/cosmetic/notice/publication/
protection delivery happens after commit through durable outbox retry. This implements the report's
Spring transaction model; incoming Quests delivery is not an asynchronous queue.

Receivers deduplicate gems by `rewardId`, cosmetics by `awardId` and protection/publication by `requestId`.
The optional achievement notice does not publish a post. Only Community's persisted post produces
`PublicationCreatedIntegrationEvent`, with request, award, requester, community and publication IDs.
Only Monetization's inventory result resolves protection. Unknown Community publication flows are
ignored; mismatched known confirmations fail. A missing supplier is a 503 for dependent REST operations,
not an empty result, denied membership or success. Outbox failures preserve pending business state.

## Integration evidence boundaries

JUnit validates the **real** Monetization services and actual Quests public events/history against H2
and can run against PostgreSQL. Community permissions, goal recognition and acknowledgements use an
explicit test mock until a real supplier is published. This is not Community end-to-end evidence.
Android ViewModels, dialog drafts, weekly ordering and Community feed animation are separate mobile/
Community responsibilities; backend completion does not imply those interfaces have been implemented.
