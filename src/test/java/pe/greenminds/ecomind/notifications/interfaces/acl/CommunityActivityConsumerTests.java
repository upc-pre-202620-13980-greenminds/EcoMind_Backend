package pe.greenminds.ecomind.notifications.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityGoalReachedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.PublicationCreatedIntegrationEvent;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.CommunityServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

@ExtendWith(MockitoExtension.class)
class CommunityActivityConsumerTests {

  private static final Long COMMUNITY = 7L;
  private static final Long AUTHOR = 1L;
  private static final Long MEMBER = 2L;

  @Mock
  private GeneratedNotificationSender sender;
  @Mock
  private CommunityServiceClient communityServiceClient;
  @Mock
  private UsersServiceClient usersServiceClient;

  private CommunityActivityConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new CommunityActivityConsumer(sender, communityServiceClient, usersServiceClient);
  }

  @Test
  void theOtherMembersAreToldThatSomeoneSharedAnAchievement() {
    when(communityServiceClient.findMemberIds(COMMUNITY)).thenReturn(List.of(AUTHOR, MEMBER));
    when(usersServiceClient.getUserName(AUTHOR)).thenReturn(Optional.of("Ana"));

    consumer.on(
        new PublicationCreatedIntegrationEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            AUTHOR,
            COMMUNITY,
            30L,
            Instant.now()));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<Long>> recipients = ArgumentCaptor.forClass(Collection.class);
    verify(sender)
        .send(
            recipients.capture(),
            eq(NotificationCategory.COMMUNITY_ACTIVITY),
            eq(NotificationType.ACHIEVEMENT_SHARED),
            eq("Ana"),
            isNull(),
            eq("PUBLICATION:30"));
    assertThat(recipients.getValue()).containsExactly(MEMBER);
  }

  @Test
  void everyMemberIsToldThatTheCommunityCompletedAGoal() {
    when(communityServiceClient.findMemberIds(COMMUNITY)).thenReturn(List.of(AUTHOR, MEMBER));

    consumer.on(
        new CommunityGoalReachedIntegrationEvent(
            UUID.randomUUID(), 40L, COMMUNITY, "Save energy", Instant.now()));

    verify(sender)
        .send(
            List.of(AUTHOR, MEMBER),
            NotificationCategory.COMMUNITY_ACTIVITY,
            NotificationType.COMMUNITY_GOAL_REACHED,
            "Save energy",
            null,
            "COMMUNITY_GOAL:40");
  }
}
