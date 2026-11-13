package pe.greenminds.ecomind.notifications.interfaces.acl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.gamification.interfaces.acl.events.AchievementUnlockedIntegrationEvent;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.GamificationServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;
import pe.greenminds.ecomind.quests.interfaces.acl.events.CollaborativeQuestInvitationSentIntegrationEvent;

@ExtendWith(MockitoExtension.class)
class QuestAndAchievementConsumerTests {

  private static final Long OWNER = 1L;
  private static final Long INVITED = 2L;

  @Mock
  private GeneratedNotificationSender sender;
  @Mock
  private UsersServiceClient usersServiceClient;
  @Mock
  private GamificationServiceClient gamificationServiceClient;

  @Test
  void theInvitedUserIsToldWhoInvitedThem() {
    var consumer = new QuestInvitationConsumer(sender, usersServiceClient);
    UUID eventId = UUID.randomUUID();
    when(usersServiceClient.getUserName(OWNER)).thenReturn(Optional.of("Ana"));

    consumer.on(
        new CollaborativeQuestInvitationSentIntegrationEvent(
            eventId, 5L, 6L, INVITED, OWNER, OffsetDateTime.now()));

    verify(sender)
        .send(
            List.of(INVITED),
            NotificationCategory.NEW_AND_DAILY_QUESTS,
            NotificationType.QUEST_INVITATION,
            "Ana",
            null,
            "QUEST_INVITATION:" + eventId);
  }

  @Test
  void theUserIsToldTheNameOfTheAchievement() {
    var consumer = new AchievementUnlockedConsumer(sender, gamificationServiceClient);
    UUID awardId = UUID.randomUUID();
    UUID achievementId = UUID.randomUUID();
    when(gamificationServiceClient.findAchievementName(achievementId))
        .thenReturn(Optional.of("First quest"));

    consumer.on(
        new AchievementUnlockedIntegrationEvent(
            UUID.randomUUID(), awardId, achievementId, INVITED, Instant.now()));

    verify(sender)
        .send(
            List.of(INVITED),
            NotificationCategory.ACHIEVEMENTS_AND_BADGES,
            NotificationType.ACHIEVEMENT_UNLOCKED,
            "First quest",
            null,
            "ACHIEVEMENT:" + awardId);
  }

  @Test
  void anUnknownAchievementIsNotNotified() {
    var consumer = new AchievementUnlockedConsumer(sender, gamificationServiceClient);
    UUID achievementId = UUID.randomUUID();
    when(gamificationServiceClient.findAchievementName(achievementId)).thenReturn(Optional.empty());

    consumer.on(
        new AchievementUnlockedIntegrationEvent(
            UUID.randomUUID(), UUID.randomUUID(), achievementId, INVITED, Instant.now()));

    verify(sender, never()).send(any(), any(), any(), anyString(), any(), anyString());
  }
}
