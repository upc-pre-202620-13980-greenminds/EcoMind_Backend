package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.FamilyRewardCommandService;
import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.GamificationQueryService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantFamilyPlanRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.FamilyId;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(
        properties =
                "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:achievementtests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AchievementTests {
    @Autowired AchievementCommandService achievements;
    @Autowired AchievementQueryService queries;
    @Autowired RewardCommandService rewards;
    @Autowired FamilyRewardCommandService familyRewards;
    @Autowired GamificationQueryService progress;
    @Autowired FamilyRepository families;
    @Autowired PlatformTransactionManager transactions;
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc http;
    @Autowired TokenService tokens;
    private static final UserId USER = new UserId(7001L);
    private static final LocalDate DAY = LocalDate.of(2026, 10, 7);

    @BeforeEach
    void clean() {
        for (String table :
                new String[] {
                    "achievement_awards",
                    "achievements",
                    "reward_transactions",
                    "user_progresses",
                    "family_scores",
                    "family_members",
                    "families"
                }) jdbc.update("DELETE FROM " + table);
    }

    @Test
    void thresholdCrossingAwardsOnceAcrossRetriesAndLaterRewards() {
        var definition =
                definition(
                        "POINTS",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        20,
                        true);
        achievements.register(definition);
        rewards.handle(command(UUID.randomUUID(), USER, 10, DAY, true));
        assertTrue(queries.forUser(USER, 0, 20).isEmpty());
        var crossing = command(UUID.randomUUID(), USER, 10, DAY, true);
        rewards.handle(crossing);
        var award = queries.forUser(USER, 0, 20).getFirst();
        rewards.handle(crossing);
        rewards.handle(command(UUID.randomUUID(), USER, 15, DAY, true));
        assertEquals(1, queries.forUser(USER, 0, 20).size());
        assertEquals(definition.id(), award.achievementId());
        assertEquals(crossing.sourceExecutionId(), award.sourceEventId());
        assertEquals(35, progress.getUserProgress(USER).getTotalEcopoints());
    }

    @Test
    void ecopointsAndStreakUseTheirCriteriaAndInactiveDefinitionsDoNotGrant() {
        achievements.register(
                definition(
                        "POINTS", AchievementScope.INDIVIDUAL, AchievementMetric.ECOPOINTS, 10, true));
        achievements.register(
                definition(
                        "STREAK",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.LONGEST_STREAK,
                        2,
                        true));
        achievements.register(
                definition(
                        "INACTIVE",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        1,
                        false));
        achievements.register(
                definition(
                        "FAMILY", AchievementScope.FAMILY, AchievementMetric.ECOPOINTS, 1, true));
        rewards.handle(command(UUID.randomUUID(), USER, 100, DAY, true));
        rewards.handle(command(UUID.randomUUID(), USER, 100, DAY, true));
        assertEquals(1, queries.forUser(USER, 0, 20).size());
        rewards.handle(command(UUID.randomUUID(), USER, 1, DAY.plusDays(1), true));
        assertEquals(2, queries.forUser(USER, 0, 20).size());
    }

    @Test
    void concurrentDistinctRewardsCannotGrantSameAchievementTwice() throws Exception {
        achievements.register(
                definition(
                        "POINTS",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        10,
                        true));
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first =
                    executor.submit(
                            () -> {
                                start.await();
                                return rewards.handle(
                                        command(UUID.randomUUID(), USER, 10, DAY, true));
                            });
            var second =
                    executor.submit(
                            () -> {
                                start.await();
                                return rewards.handle(
                                        command(UUID.randomUUID(), USER, 10, DAY, true));
                            });
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
        }
        assertEquals(1, queries.forUser(USER, 0, 20).size());
        assertEquals(20, progress.getUserProgress(USER).getTotalEcopoints());
    }

    @Test
    void familyAwardsRequireCurrentMembershipAndDoNotAppearInPersonalAwards() throws Exception {
        var family =
                new TransactionTemplate(transactions)
                        .execute(
                                status ->
                                        families.save(
                                                Family.create(
                                                        new pe.greenminds.ecomind.users.domain.model
                                                                .valueobjects.UserId(USER.value()),
                                                        "Family",
                                                        "Save energy")));
        var familyId = new FamilyId(family.getId().value());
        achievements.register(
                definition(
                        "FAMILY", AchievementScope.FAMILY, AchievementMetric.ECOPOINTS, 30, true));
        familyRewards.handle(
                new GrantFamilyPlanRewardCommand(UUID.randomUUID(), familyId, 30, Instant.now()));
        assertTrue(queries.forUser(new UserId(familyId.value()), 0, 20).isEmpty());
        String path = "/api/v1/gamification/families/" + familyId.value() + "/achievements";
        http.perform(get(path).header("Authorization", bearer(USER.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].scope").value("FAMILY"));
        http.perform(get(path).header("Authorization", bearer(7002L)))
                .andExpect(status().isForbidden());
        new TransactionTemplate(transactions)
                .executeWithoutResult(
                        status -> {
                            family.removeMember(family.getMembers().getFirst().getId());
                            families.save(family);
                        });
        http.perform(get(path).header("Authorization", bearer(USER.value())))
                .andExpect(status().isForbidden());
    }

    @Test
    void catalogSupportsScopePaginationDetailAndValidatedInputs() throws Exception {
        var definition =
                definition("A", AchievementScope.INDIVIDUAL, AchievementMetric.ECOPOINTS, 5, true);
        achievements.register(definition);
        achievements.register(
                definition("B", AchievementScope.FAMILY, AchievementMetric.ECOPOINTS, 5, true));
        String path = "/api/v1/gamification/achievements";
        http.perform(get(path)).andExpect(status().isUnauthorized());
        http.perform(
                        get(path)
                                .param("scope", "FAMILY")
                                .header("Authorization", bearer(USER.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value("B"));
        http.perform(
                        get(path)
                                .param("size", "1")
                                .param("page", "1")
                                .header("Authorization", bearer(USER.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("B"));
        http.perform(
                        get(path + "/" + definition.id())
                                .header("Authorization", bearer(USER.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.target").value(5));
        http.perform(
                        get(path + "/" + UUID.randomUUID())
                                .header("Authorization", bearer(USER.value())))
                .andExpect(status().isNotFound());
        for (String size : new String[] {"0", "101"})
            http.perform(
                            get(path)
                                    .param("size", size)
                                    .header("Authorization", bearer(USER.value())))
                    .andExpect(status().isBadRequest());
        http.perform(get(path).param("page", "-1").header("Authorization", bearer(USER.value())))
                .andExpect(status().isBadRequest());
        http.perform(
                        get(path)
                                .param("scope", "INVALID")
                                .header("Authorization", bearer(USER.value())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void personalAwardsUseJwtIdentity() throws Exception {
        achievements.register(
                definition(
                        "POINTS",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        1,
                        true));
        rewards.handle(command(UUID.randomUUID(), USER, 1, DAY, true));
        String path = "/api/v1/gamification/me/achievements";
        http.perform(get(path).header("Authorization", bearer(USER.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        http.perform(get(path).header("Authorization", bearer(7002L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void rewardProgressAndAwardRollBackTogether() {
        achievements.register(
                definition(
                        "POINTS",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        1,
                        true));
        assertThrows(
                IllegalStateException.class,
                () ->
                        new TransactionTemplate(transactions)
                                .executeWithoutResult(
                                        status -> {
                                            rewards.handle(
                                                    command(UUID.randomUUID(), USER, 1, DAY, true));
                                            assertEquals(1, queries.forUser(USER, 0, 20).size());
                                            throw new IllegalStateException(
                                                    "Simulate failure in the completion"
                                                            + " transaction");
                                        }));
        assertTrue(queries.forUser(USER, 0, 20).isEmpty());
        assertTrue(progress.getRecentRewards(USER).isEmpty());
        assertEquals(0, progress.getUserProgress(USER).getTotalEcopoints());
    }

    @Test
    void catalogRejectsInvalidTargetsIncompatibleScopesAndDuplicateCodes() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        definition(
                                "ZERO",
                                AchievementScope.INDIVIDUAL,
                                AchievementMetric.ECOPOINTS,
                                0,
                                true));
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        definition(
                                "STREAK",
                                AchievementScope.FAMILY,
                                AchievementMetric.LONGEST_STREAK,
                                1,
                                true));
        var definition =
                definition(
                        "UNIQUE",
                        AchievementScope.INDIVIDUAL,
                        AchievementMetric.ECOPOINTS,
                        1,
                        true);
        achievements.register(definition);
        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        achievements.register(
                                definition(
                                        "UNIQUE",
                                        AchievementScope.INDIVIDUAL,
                                        AchievementMetric.ECOPOINTS,
                                        2,
                                        true)));
        assertEquals(1, queries.search(null, 0, 20).size());
    }

    private Achievement definition(
            String code,
            AchievementScope scope,
            AchievementMetric metric,
            long target,
            boolean active) {
        return new Achievement(
                UUID.randomUUID(),
                code,
                code,
                "Test-configured criterion",
                scope,
                metric,
                target,
                active);
    }

    private GrantQuestRewardCommand command(
            UUID execution, UserId user, long points, LocalDate date, boolean daily) {
        return new GrantQuestRewardCommand(
                execution,
                user,
                date.atStartOfDay().toInstant(java.time.ZoneOffset.UTC),
                date,
                daily,
                new Reward(points, 0));
    }

    private String bearer(long id) {
        return "Bearer "
                + tokens.issueAccessToken(
                                new AuthenticatedUser(
                                        new AccountId(id), new EmailAddress(id + "@example.com")))
                        .value();
    }
}
