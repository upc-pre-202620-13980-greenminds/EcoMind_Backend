package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.RankingQueryService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.aggregates.Friendship;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;
import pe.greenminds.ecomind.users.domain.repositories.FriendshipRepository;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(
        properties =
                "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:rankingtests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_AND_AFTER_CLASS)
class RankingTests {
    @Autowired RankingQueryService queries;
    @Autowired RewardCommandService rewards;
    @Autowired FamilyRewardCommandService familyRewards;
    @Autowired UserProfileRepository profiles;
    @Autowired FamilyRepository families;
    @Autowired FriendshipRepository friendships;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;
    @Autowired MockMvc http;
    @Autowired TokenService tokens;
    static final UserId SELF = new UserId(8101L);
    static final Instant FROM = Instant.parse("2026-10-05T05:00:00Z");
    static final Instant TO = Instant.parse("2026-10-12T05:00:00Z");
    static final RankingPeriod PERIOD = new RankingPeriod(FROM, TO);

    @BeforeEach
    void setup() {
        for (String table :
                new String[] {
                    "achievement_awards",
                    "achievements",
                    "reward_transactions",
                    "user_progresses",
                    "family_scores",
                    "friendships",
                    "family_members",
                    "families",
                    "user_profiles"
                }) jdbc.update("DELETE FROM " + table);
        for (long id = 8101; id <= 8105; id++) createProfile(id);
    }

    @Test
    void globalParticipantsUseGamificationScoresAndIncludeZeroProgressWithoutWriting() {
        grant(SELF.value(), 12, FROM);
        grant(9999L, 500, FROM); // A stale/orphaned reference is not a directory participant.
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status -> {
                            var profile = profiles.findById(user(SELF.value())).orElseThrow();
                            profile.updateProgress(10, LocalDate.of(2026, 10, 7), 999, 100);
                            profiles.save(profile);
                        });
        var page = queries.participants(RankingType.GLOBAL, SELF, 0, 2);
        assertEquals(2, page.items().size());
        assertTrue(page.hasNext());
        assertEquals(new RankingEntry(8101L, "User 8101", 12), page.items().getFirst());
        assertEquals(0, page.items().get(1).totalEcopoints());
        assertEquals(1, queries.participants(RankingType.GLOBAL, SELF, 2, 2).items().size());
        assertFalse(queries.participants(RankingType.GLOBAL, SELF, 2, 2).hasNext());
        assertTrue(queries.participants(RankingType.GLOBAL, SELF, 20, 2).items().isEmpty());
        assertEquals(
                1, queries.transactions(RankingType.GLOBAL, SELF, PERIOD, 0, 20).items().size());
        assertEquals(
                2, jdbc.queryForObject("SELECT COUNT(*) FROM reward_transactions", Integer.class));
        assertEquals(
                0, jdbc.queryForObject("SELECT COUNT(*) FROM achievement_awards", Integer.class));
    }

    @Test
    void friendsIncludesSelfAndAcceptedRelationshipsInEitherDirectionOnly() throws Exception {
        connect(8101, 8102, "ACCEPTED");
        connect(8103, 8101, "ACCEPTED");
        connect(8101, 8104, "PENDING");
        connect(8105, 8101, "REJECTED");
        for (long id = 8101; id <= 8105; id++) grant(id, 5, FROM);
        assertEquals(
                List.of(8101L, 8102L, 8103L),
                queries.participants(RankingType.FRIENDS, SELF, 0, 20).items().stream()
                        .map(RankingEntry::beneficiaryId)
                        .toList());
        assertEquals(
                3, queries.transactions(RankingType.FRIENDS, SELF, PERIOD, 0, 20).items().size());
        http.perform(
                        get("/api/v1/gamification/rankings/FRIENDS/participants")
                                .param("userId", "8101")
                                .header("Authorization", bearer(8104L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].beneficiaryId").value(8104));
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status ->
                                friendships.delete(
                                        friendships
                                                .findBetween(user(8101), user(8102))
                                                .orElseThrow()));
        assertEquals(
                2, queries.transactions(RankingType.FRIENDS, SELF, PERIOD, 0, 20).items().size());
    }

    @Test
    void periodIncludesStartExcludesEndAndAdjacentPeriodsDoNotOverlap() {
        grant(SELF.value(), 100, FROM.minusSeconds(1));
        grant(SELF.value(), 2, FROM);
        grant(SELF.value(), 3, TO.minusSeconds(1));
        grant(SELF.value(), 7, TO);
        var entries = queries.transactions(RankingType.GLOBAL, SELF, PERIOD, 0, 20).items();
        assertEquals(5, entries.stream().mapToLong(RankingTransaction::ecopoints).sum());
        var next =
                queries.transactions(
                                RankingType.GLOBAL,
                                SELF,
                                new RankingPeriod(TO, TO.plusSeconds(1)),
                                0,
                                20)
                        .items();
        assertEquals(7, next.getFirst().ecopoints());
        assertTrue(entries.stream().noneMatch(entry -> entry.id().equals(next.getFirst().id())));
    }

    @Test
    void allTransactionsCanBeReadBeyondOneHundredWithStableTieOrdering() {
        for (int i = 0; i < 103; i++) grant(SELF.value(), 1, FROM);
        var collected = new ArrayList<RankingTransaction>();
        var first = queries.transactions(RankingType.GLOBAL, SELF, PERIOD, 0, 100);
        assertTrue(first.hasNext());
        collected.addAll(first.items());
        var second = queries.transactions(RankingType.GLOBAL, SELF, PERIOD, 1, 100);
        assertFalse(second.hasNext());
        collected.addAll(second.items());
        assertEquals(103, collected.stream().map(RankingTransaction::id).distinct().count());
        assertEquals(103, collected.stream().mapToLong(RankingTransaction::ecopoints).sum());
    }

    @Test
    void familyRankingsUseOnlyFamilyRewardsEvenWhenUserIdentityMatches() {
        var family =
                new TransactionTemplate(transactions)
                        .execute(
                                status ->
                                        families.save(
                                                Family.create(
                                                        user(SELF.value()),
                                                        "Green family",
                                                        "Save water")));
        long id = family.getId().value();
        createProfile(id);
        grant(id, 90, FROM);
        familyRewards.handle(
                new GrantFamilyPlanRewardCommand(UUID.randomUUID(), new FamilyId(id), 25, FROM));
        var entry = queries.participants(RankingType.FAMILIES, SELF, 0, 20).items().getFirst();
        assertEquals("Green family", entry.displayName());
        assertEquals(25, entry.totalEcopoints());
        var entries = queries.transactions(RankingType.FAMILIES, SELF, PERIOD, 0, 20).items();
        assertEquals(1, entries.size());
        assertEquals(25, entries.getFirst().ecopoints());
        assertEquals(
                90,
                queries.transactions(RankingType.GLOBAL, SELF, PERIOD, 0, 20)
                        .items()
                        .getFirst()
                        .ecopoints());
    }

    @Test
    void endpointsRequireJwtAndRejectUnsupportedTypesInvalidPeriodsAndPagination()
            throws Exception {
        String base = "/api/v1/gamification/rankings";
        http.perform(get(base + "/types")).andExpect(status().isUnauthorized());
        http.perform(get(base + "/types").header("Authorization", bearer(SELF.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
        http.perform(
                        get(base + "/UNKNOWN/participants")
                                .header("Authorization", bearer(SELF.value())))
                .andExpect(status().isBadRequest());
        for (String size : new String[] {"0", "101"})
            http.perform(
                            get(base + "/GLOBAL/participants")
                                    .param("size", size)
                                    .header("Authorization", bearer(SELF.value())))
                    .andExpect(status().isBadRequest());
        http.perform(
                        get(base + "/GLOBAL/participants")
                                .param("page", "-1")
                                .header("Authorization", bearer(SELF.value())))
                .andExpect(status().isBadRequest());
        http.perform(
                        get(base + "/GLOBAL/transactions")
                                .header("Authorization", bearer(SELF.value())))
                .andExpect(status().isBadRequest());
        http.perform(
                        get(base + "/GLOBAL/transactions")
                                .param("from", TO.toString())
                                .param("to", FROM.toString())
                                .header("Authorization", bearer(SELF.value())))
                .andExpect(status().isBadRequest());
        http.perform(
                        get(base + "/GLOBAL/transactions")
                                .param("from", FROM.toString())
                                .param("to", TO.toString())
                                .header("Authorization", bearer(SELF.value())))
                .andExpect(status().isOk());
    }

    private void createProfile(long id) {
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status ->
                                profiles.save(
                                        UserProfile.create(
                                                user(id), "User " + id, SocialRole.PARENT)));
    }

    private pe.greenminds.ecomind.users.domain.model.valueobjects.UserId user(long id) {
        return new pe.greenminds.ecomind.users.domain.model.valueobjects.UserId(id);
    }

    private void connect(long from, long to, String status) {
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        transaction -> {
                            var friendship = Friendship.request(user(from), user(to));
                            if (status.equals("ACCEPTED")) friendship.accept();
                            if (status.equals("REJECTED")) friendship.reject();
                            friendships.save(friendship);
                        });
    }

    private void grant(long id, long points, Instant occurredAt) {
        rewards.handle(
                new GrantQuestRewardCommand(
                        UUID.randomUUID(),
                        new UserId(id),
                        occurredAt,
                        LocalDate.of(2026, 10, 7),
                        false,
                        new Reward(points, 0)));
    }

    private String bearer(long id) {
        return "Bearer "
                + tokens.issueAccessToken(
                                new AuthenticatedUser(
                                        new AccountId(id), new EmailAddress(id + "@example.com")))
                        .value();
    }
}
