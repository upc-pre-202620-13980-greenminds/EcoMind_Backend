# Gamification integration contracts

This change creates the public boundaries needed by report sections 2.6.6 and 2.6.7. It does not
implement the Quests completion workflow, Community memberships/feed or Monetization wallet/inventory.
External user and family identities follow the active backend's numeric IDs; execution, request,
award, quest, plan, community and publication references in these proposed contracts use UUIDs.

## Structure and state

| Owner | Public contract | Current state |
|---|---|---|
| Quests | `QuestCompletedIntegrationEvent`, `FamilyPlanCompletedIntegrationEvent` | Connected to existing Gamification command services via `QuestCompletionConsumer` |
| Quests | `QuestEventPublisher` / `SpringQuestEventPublisher` | Working in-process publisher for the two connected event types; requires an existing transaction |
| Quests | `MinigameCompletedIntegrationEvent`, `CollaborativeQuestCompletedIntegrationEvent` | Validated message shapes only; no publisher/consumer path yet |
| Quests | `QuestsContextFacade` | Interface for validated attempt data and repetition history; requires a real implementation |
| Monetization | `MonetizationContextFacade` | Interface for XP multiplier lookup, idempotent gem/cosmetic grants and protector requests |
| Monetization | `StreakProtectedIntegrationEvent`, `StreakProtectionUnavailableIntegrationEvent` | Correlated result contracts; inventory handling and consumption still pending |
| Community | `CommunityContextFacade` | Interface for local community, memberships, publishing permissions, achievement notices and publication requests |
| Community | `CommunityGoalCompletedIntegrationEvent`, `PublicationCreatedIntegrationEvent` | Goal completion and correlated publication acknowledgement contracts |
| Gamification | `GamificationContextFacade` | Working read-only progress snapshot for other contexts; does not claim a stored streak is currently eligible for protection |
| Gamification | `QuestServiceClient`, `MonetizationServiceClient`, `CommunityServiceClient` and ACL implementations | Resolve public supplier implementations when registered; throw explicitly while missing |
| Gamification | Reward, streak risk, achievement notice and share-request integration events | Outgoing message contracts; not yet emitted by the current services |
| Community, Monetization, Gamification | `*EventPublisher` ports | Interfaces for future transactional outbox implementations; no dummy publisher beans |

Only public ACL types cross context boundaries. There are no imports of another context's domain or
persistence classes and no new HTTP endpoints that let mobile clients forge completion or grant events.

## Quests delivery semantics

Quests must first validate/persist completion and publish from its transaction. The event carries both
a delivery `eventId` and a stable canonical execution ID. Gamification deduplicates by execution and
beneficiary even if a retry has a different event ID. It translates base amounts into its own Reward
and UserId/FamilyId model; the source never imports Gamification internals.

The current implementation follows the explicit Spring Application Events transaction model in
section 2.6.6: listeners run synchronously and a listener failure rolls back the completion transaction.
The context-map rationale describes eventual asynchronous delivery; that is not implemented here.
A future durable asynchronous path needs an outbox and retry policy before changing these semantics.

Family events contain only the configured additional bonus. Their participant list is context for the
validated completion, not a command to grant individual rewards again. Missing families cause failure.
Gem-bearing quest events still fail until real Monetization delivery is implemented; this does not
pretend that gems were credited.

## Correlation and supplier responsibilities

- Monetization deduplicates gem grants by `rewardId`, cosmetic grants by `awardId` and protector
  consumption by `requestId`. Only actual lack of inventory produces an unavailable result. Technical
  errors remain retryable. XP multiplier validity uses an inclusive start and exclusive end.
- Community distinguishes an achievement notice from a voluntary publish request. Only a persisted
  post produces `PublicationCreatedIntegrationEvent`, preserving `requestId`, `awardId`, requester and
  community. Sending a request does not mark an achievement as published.
- Producers/consumers of outgoing integration events must implement durable storage, deduplication,
  permissions and acknowledgements. The public contracts alone do not provide those guarantees.
- Missing suppliers raise `IllegalStateException`; they are never treated as an empty catalog, absent
  multiplier, denied membership or successful wallet/publication operation. The existing application
  boots because it does not invoke these unfinished integrations yet.

## Validation

JUnit verifies transactional event delivery, re-delivery with a new message ID, atomic rollback on a
consumer failure, family bonus isolation, required transaction boundaries, immutable/unique participant
lists, multiplier boundaries and required publication correlation. Test database: H2 in MySQL mode.
