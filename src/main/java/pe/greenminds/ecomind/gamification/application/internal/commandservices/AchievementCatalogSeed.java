package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import java.nio.charset.StandardCharsets;
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
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository;

@Component
@Profile("prod")
@ConditionalOnProperty(name = "gamification.catalog.enabled", havingValue = "true", matchIfMissing = true)
public class AchievementCatalogSeed implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(AchievementCatalogSeed.class);

    private static final List<Achievement> DEFINITIONS = List.of(
            definition("FIRST_ECOPOINTS", "First ecopoints",
                    "Earn at least one ecopoint from eligible actions.",
                    AchievementScope.INDIVIDUAL, AchievementMetric.ECOPOINTS, 1),
            definition("SEVEN_DAY_STREAK", "Seven-day streak",
                    "Reach a seven-day streak by completing eligible daily quests.",
                    AchievementScope.INDIVIDUAL, AchievementMetric.LONGEST_STREAK, 7),
            definition("FIRST_FAMILY_PLAN", "Family collaboration",
                    "Complete your first family plan together.",
                    AchievementScope.FAMILY, AchievementMetric.COMPLETED_FAMILY_PLANS, 1));

    private final AchievementRepository catalog;
    private final AchievementCommandService commands;

    public AchievementCatalogSeed(AchievementRepository catalog, AchievementCommandService commands) {
        this.catalog = catalog;
        this.commands = commands;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void run(ApplicationArguments args) {
        catalog.lockCatalog();
        int created = 0;
        for (var definition : DEFINITIONS) {
            if (catalog.existsByCodeOrId(definition.code(), definition.id())) continue;
            commands.register(definition);
            created++;
        }
        LOGGER.info("Initialized {} achievement definitions; existing definitions retained", created);
    }

    private static Achievement definition(String code, String name, String description,
            AchievementScope scope, AchievementMetric metric, long target) {
        var id = UUID.nameUUIDFromBytes(("ecomind:achievement:" + code).getBytes(StandardCharsets.UTF_8));
        return new Achievement(id, code, name, description, scope, metric, target, true);
    }
}
