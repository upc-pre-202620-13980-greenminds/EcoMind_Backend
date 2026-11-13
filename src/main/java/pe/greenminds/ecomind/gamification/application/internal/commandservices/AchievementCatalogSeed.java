package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.commands.AwardAchievementCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;
import pe.greenminds.ecomind.gamification.domain.repositories.RewardTransactionRepository;

@Component
@Profile("prod")
@ConditionalOnProperty(name = "gamification.catalog.enabled", havingValue = "true", matchIfMissing = true)
public class AchievementCatalogSeed implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AchievementCatalogSeed.class);
    private static final String FIRST_QUEST = "FIRST_QUEST_COMPLETED";

    private static final List<Achievement> DEFINITIONS = List.of(
            definition(FIRST_QUEST, "First quest completed",
                    "Complete your first quest successfully.",
                    AchievementScope.INDIVIDUAL, AchievementMetric.COMPLETED_QUESTS, 1),
            definition("SEVEN_DAY_STREAK", "Seven-day streak",
                    "Reach a seven-day streak by completing eligible daily quests.",
                    AchievementScope.INDIVIDUAL, AchievementMetric.LONGEST_STREAK, 7),
            definition("FIRST_FAMILY_PLAN", "Family collaboration",
                    "Complete your first family plan together.",
                    AchievementScope.FAMILY, AchievementMetric.COMPLETED_FAMILY_PLANS, 1));

    private final AchievementRepository catalog;
    private final AchievementCommandService commands;
    private final RewardTransactionRepository rewards;

    public AchievementCatalogSeed(AchievementRepository catalog, AchievementCommandService commands,
            RewardTransactionRepository rewards) {
        this.catalog = catalog;
        this.commands = commands;
        this.rewards = rewards;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void run(ApplicationArguments args) {
        catalog.lockCatalog();
        catalog.deactivate("FIRST_ECOPOINTS", definitionId("FIRST_ECOPOINTS"));
        int created = 0;
        boolean firstQuestCreated = false;
        for (var definition : DEFINITIONS) {
            if (catalog.existsByCodeOrId(definition.code(), definition.id())) continue;
            commands.register(definition);
            if (definition.code().equals(FIRST_QUEST)) firstQuestCreated = true;
            created++;
        }
        if (firstQuestCreated) {
            var achievementId = definitionId(FIRST_QUEST);
            var now = Instant.now();
            for (var user : rewards.findUsersWithCompletedQuests()) {
                commands.handle(new AwardAchievementCommand(
                        achievementId, user.value(), null, achievementId, now));
            }
        }
        LOGGER.info("Initialized {} achievement definitions; existing definitions retained", created);
    }

    private static Achievement definition(String code, String name, String description,
            AchievementScope scope, AchievementMetric metric, long target) {
        return new Achievement(definitionId(code), code, name, description, scope, metric, target, true);
    }

    private static UUID definitionId(String code) {
        return UUID.nameUUIDFromBytes(("ecomind:achievement:" + code).getBytes(StandardCharsets.UTF_8));
    }
}
