package pe.greenminds.ecomind.notifications.interfaces.acl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventPublishedIntegrationEvent;
import pe.greenminds.ecomind.community.interfaces.acl.events.CommunityEventPublishedIntegrationEvent.Action;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.CommunityServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.outboundservices.acl.UsersServiceClient;
import pe.greenminds.ecomind.notifications.application.internal.services.GeneratedNotificationSender;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationCategory;
import pe.greenminds.ecomind.notifications.domain.model.valueobjects.NotificationType;

@ExtendWith(MockitoExtension.class)
class CommunityEventConsumerTests {

  private static final Long COMMUNITY = 7L;
  private static final Long AUTHOR = 1L;
  private static final Long STUDENT_MEMBER = 2L;
  private static final Long PARENT_OF_STUDENT = 3L;
  private static final Long PARENT_MEMBER = 4L;

  @Mock
  private GeneratedNotificationSender sender;
  @Mock
  private CommunityServiceClient communityServiceClient;
  @Mock
  private UsersServiceClient usersServiceClient;

  private CommunityEventConsumer consumer;

  @BeforeEach
  void setUp() {
    consumer = new CommunityEventConsumer(sender, communityServiceClient, usersServiceClient);
  }

  @Test
  void onlyParentsOfTheCommunityAndOfTheFamiliesOfItsMembersAreAlerted() {
    when(communityServiceClient.findMemberIds(COMMUNITY))
        .thenReturn(List.of(AUTHOR, STUDENT_MEMBER, PARENT_MEMBER));
    when(usersServiceClient.getUserAndFamilyMemberIds(AUTHOR)).thenReturn(Set.of(AUTHOR));
    when(usersServiceClient.getUserAndFamilyMemberIds(STUDENT_MEMBER))
        .thenReturn(Set.of(STUDENT_MEMBER, PARENT_OF_STUDENT));
    when(usersServiceClient.getUserAndFamilyMemberIds(PARENT_MEMBER))
        .thenReturn(Set.of(PARENT_MEMBER));
    when(usersServiceClient.isParent(STUDENT_MEMBER)).thenReturn(false);
    when(usersServiceClient.isParent(PARENT_OF_STUDENT)).thenReturn(true);
    when(usersServiceClient.isParent(PARENT_MEMBER)).thenReturn(true);

    consumer.on(event(Action.CREATED));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Collection<Long>> recipients = ArgumentCaptor.forClass(Collection.class);
    verify(sender)
        .send(
            recipients.capture(),
            eq(NotificationCategory.COMMUNITY_EVENTS),
            eq(NotificationType.COMMUNITY_EVENT_CREATED),
            eq("Park cleanup"),
            isNull(),
            eq("COMMUNITY_EVENT_CREATED:20"));
    assertThat(recipients.getValue()).containsExactlyInAnyOrder(PARENT_OF_STUDENT, PARENT_MEMBER);
  }

  @Test
  void anUpdateUsesItsOwnTypeAndKey() {
    when(communityServiceClient.findMemberIds(COMMUNITY)).thenReturn(List.of(PARENT_MEMBER));
    when(usersServiceClient.getUserAndFamilyMemberIds(PARENT_MEMBER))
        .thenReturn(Set.of(PARENT_MEMBER));
    when(usersServiceClient.isParent(PARENT_MEMBER)).thenReturn(true);

    consumer.on(event(Action.UPDATED));

    ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
    verify(sender)
        .send(
            any(),
            eq(NotificationCategory.COMMUNITY_EVENTS),
            eq(NotificationType.COMMUNITY_EVENT_UPDATED),
            anyString(),
            isNull(),
            key.capture());
    assertThat(key.getValue()).startsWith("COMMUNITY_EVENT_UPDATED:20:");
  }

  private static CommunityEventPublishedIntegrationEvent event(Action action) {
    return new CommunityEventPublishedIntegrationEvent(
        UUID.randomUUID(), 20L, COMMUNITY, AUTHOR, "Park cleanup", action, Instant.now());
  }
}
