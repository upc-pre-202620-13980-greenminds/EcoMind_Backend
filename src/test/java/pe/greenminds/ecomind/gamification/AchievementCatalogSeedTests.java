package pe.greenminds.ecomind.gamification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.application.internal.commandservices.AchievementCatalogSeed;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;

@SpringBootTest(properties = "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:achievementseedtests;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}")
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AchievementCatalogSeedTests {
    private static final int PAGE_SIZE = 100;

    @Autowired AchievementRepository catalog;
    @Autowired AchievementCommandService commands;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager transactions;

    @BeforeEach
    void clean() {
        jdbc.update("DELETE FROM achievement_share_requests");
        jdbc.update("DELETE FROM achievement_awards");
        jdbc.update("DELETE FROM achievements");
    }

    @Test
    void preservesExistingDefinitionsAndLargeCatalogAcrossRestarts() throws Exception {
        // This user-maintained, inactive definition has a canonical seed code. It must be
        // retained as-is rather than activated or overwritten by the production initializer.
        var curated = definition("FIRST_ECOPOINTS", false);
        commands.register(curated);

        // A substantial existing catalog must remain intact while defaults are added.
        for (int i = 0; i < PAGE_SIZE; i++) {
            commands.register(new Achievement(
                    UUID.randomUUID(), String.format("A_CUSTOM_%03d", i), "Custom " + i,
                    "Existing custom achievement definition", AchievementScope.INDIVIDUAL,
                    AchievementMetric.ECOPOINTS, i + 1L, i % 2 == 0));
        }

        var seed = new AchievementCatalogSeed(catalog, commands);
        var args = new DefaultApplicationArguments(new String[0]);
        runInTransaction(seed, args);

        var allAfterFirstRun = catalog.search(null, 0, 200);
        assertEquals(103, allAfterFirstRun.size());
        assertTrue(allAfterFirstRun.stream().anyMatch(a -> a.code().equals("SEVEN_DAY_STREAK")));
        assertTrue(allAfterFirstRun.stream().anyMatch(a -> a.code().equals("FIRST_FAMILY_PLAN")));
        var retained = allAfterFirstRun.stream()
                .filter(a -> a.code().equals("FIRST_ECOPOINTS"))
                .findFirst().orElseThrow();
        assertEquals(curated, retained);
        assertFalse(retained.active());

        runInTransaction(seed, args);
        assertEquals(103, catalog.search(null, 0, 200).size(), "restarting the initializer is idempotent");
    }

    @Test
    void seedsTheThreeReportBackedCriteriaWithoutChangingTheirTargets() throws Exception {
        runInTransaction(
                new AchievementCatalogSeed(catalog, commands),
                new DefaultApplicationArguments(new String[0]));

        var definitions = new ArrayList<Achievement>();
        for (int page = 0; ; page++) {
            var batch = catalog.search(null, page, 2);
            definitions.addAll(batch);
            if (batch.size() < 2) break;
        }

        assertEquals(3, definitions.size());
        assertEquals(1, definitions.stream().filter(a -> a.code().equals("FIRST_ECOPOINTS")
                && a.scope() == AchievementScope.INDIVIDUAL
                && a.metric() == AchievementMetric.ECOPOINTS && a.target() == 1).count());
        assertEquals(1, definitions.stream().filter(a -> a.code().equals("SEVEN_DAY_STREAK")
                && a.scope() == AchievementScope.INDIVIDUAL
                && a.metric() == AchievementMetric.LONGEST_STREAK && a.target() == 7).count());
        assertEquals(1, definitions.stream().filter(a -> a.code().equals("FIRST_FAMILY_PLAN")
                && a.scope() == AchievementScope.FAMILY
                && a.metric() == AchievementMetric.COMPLETED_FAMILY_PLANS && a.target() == 1).count());
    }

    @Test
    void concurrentInitializersAreIdempotent() throws Exception {
        var seed = new AchievementCatalogSeed(catalog, commands);
        var args = new DefaultApplicationArguments(new String[0]);
        boolean postgres;
        try (var connection = jdbc.getDataSource().getConnection()) {
            postgres = connection.getMetaData().getDatabaseProductName().equals("PostgreSQL");
        }
        if (!postgres) {
            runInTransaction(seed, args);
            runInTransaction(seed, args);
        } else {
            var ready = new CountDownLatch(2);
            var start = new CountDownLatch(1);
            try (var executor = Executors.newFixedThreadPool(2)) {
                var first = executor.submit(() -> {
                    concurrentRun(seed, args, ready, start);
                    return null;
                });
                var second = executor.submit(() -> {
                    concurrentRun(seed, args, ready, start);
                    return null;
                });
                assertTrue(ready.await(5, TimeUnit.SECONDS));
                start.countDown();
                first.get(35, TimeUnit.SECONDS);
                second.get(35, TimeUnit.SECONDS);
            }
        }
        assertEquals(3, catalog.search(null, 0, 10).size());
    }

    private static Achievement definition(String code, boolean active) {
        var id = UUID.nameUUIDFromBytes(("test:achievement:" + code).getBytes(StandardCharsets.UTF_8));
        return new Achievement(id, code, "Curated definition", "Preserve this existing definition",
                AchievementScope.INDIVIDUAL, AchievementMetric.ECOPOINTS, 99, active);
    }

    private void runInTransaction(AchievementCatalogSeed seed, DefaultApplicationArguments args) {
        new TransactionTemplate(transactions).executeWithoutResult(status -> seed.run(args));
    }

    private void concurrentRun(
            AchievementCatalogSeed seed,
            DefaultApplicationArguments args,
            CountDownLatch ready,
            CountDownLatch start) throws Exception {
        ready.countDown();
        assertTrue(start.await(5, TimeUnit.SECONDS));
        runInTransaction(seed, args);
    }
}
