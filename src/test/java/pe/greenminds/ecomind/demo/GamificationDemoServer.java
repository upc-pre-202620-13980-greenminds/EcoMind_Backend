package pe.greenminds.ecomind.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import pe.greenminds.ecomind.EcomindApplication;
import pe.greenminds.ecomind.iam.domain.model.aggregates.Account;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;
import pe.greenminds.ecomind.iam.domain.repositories.AccountRepository;
import pe.greenminds.ecomind.iam.domain.services.PasswordHasher;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.application.commandservices.FamilyCommandService;
import pe.greenminds.ecomind.users.domain.model.commands.CreateFamilyCommand;
import pe.greenminds.ecomind.community.application.commandservices.CommunityCommandService;
import pe.greenminds.ecomind.community.domain.model.commands.CreateLocalCommunityCommand;
import pe.greenminds.ecomind.gamification.application.commandservices.AchievementCommandService;
import pe.greenminds.ecomind.gamification.domain.model.aggregates.Achievement;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementMetric;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.quests.application.commandservices.QuestCommandService;
import pe.greenminds.ecomind.quests.application.commandservices.ActivityCommandService;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.CreateActivityCommand;
import pe.greenminds.ecomind.quests.domain.model.commands.PublishQuestCommand;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/** Explicit test-only server with fictional accounts and a fresh, in-memory database.
 * It seeds definitions, not completion or reward transactions. Use real public REST actions
 * from Android to earn the award. Never register this launcher in production.
 */
public final class GamificationDemoServer {
    public static void main(String[] args) {
        if (args.length != 0) throw new IllegalArgumentException("This fixture accepts no configuration overrides");
        var app = new SpringApplication(EcomindApplication.class);
        app.setAdditionalProfiles("test");
        app.setDefaultProperties(Map.of(
                "server.port", "8095",
                "spring.profiles.active", "test",
                "spring.datasource.url", "jdbc:h2:mem:gamificationdemo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "gamification.outbox.enabled", "true"));
        var context = app.run(
                "--server.port=8095", "--spring.profiles.active=test", "--gamification.outbox.enabled=true",
                "--spring.datasource.url=jdbc:h2:mem:gamificationdemo;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa", "--spring.datasource.password=");
        var tx = new TransactionTemplate(context.getBean(PlatformTransactionManager.class));
        Long userId = tx.execute(status -> {
            var account = Account.create(new EmailAddress("qa.parent@example.test"), context.getBean(PasswordHasher.class).hash("QaGreen2026"));
            account.activate();
            var saved = context.getBean(AccountRepository.class).save(account);
            context.getBean(UserProfileRepository.class).save(UserProfile.create(
                    new pe.greenminds.ecomind.users.domain.model.valueobjects.UserId(saved.getId().value()), "QA Parent", SocialRole.PARENT));
            return saved.getId().value();
        });
        context.getBean(FamilyCommandService.class).handle(new CreateFamilyCommand(
                new pe.greenminds.ecomind.users.domain.model.valueobjects.UserId(userId), "QA Family", "Learn together")).toOptional().orElseThrow();
        context.getBean(CommunityCommandService.class).handle(new CreateLocalCommunityCommand(
                "QA Garden", "Fictional community for integration checks", "QA locality", null, userId)).toOptional().orElseThrow();
        context.getBean(AchievementCommandService.class).register(new Achievement(
                UUID.fromString("00000000-0000-0000-0000-000000000101"), "QA_FIRST_ACTIVITY", "First activity", "Complete an eligible activity.",
                AchievementScope.INDIVIDUAL, AchievementMetric.ECOPOINTS, 10, true));
        var quests = context.getBean(QuestCommandService.class);
        var quest = quests.handle(new CreateQuestCommand(null, Category.ENERGY, "Check unused lights", "Check whether lights are needed in an empty room.",
                null, QuestType.DAILY_QUEST, 2, 10, 1, Theme.CHECKBOX, 8, LocalDate.now(ZoneId.of("America/Lima")))).toOptional().orElseThrow();
        context.getBean(ActivityCommandService.class).handle(new CreateActivityCommand(quest.getId(), "I checked the lights in an empty room", 1, ActivityType.CHECKBOX, null, null)).toOptional().orElseThrow();
        quests.handle(new PublishQuestCommand(quest.getId())).toOptional().orElseThrow();
        System.out.println("QA_READY user=" + userId + " quest=" + quest.getId());
    }
}
