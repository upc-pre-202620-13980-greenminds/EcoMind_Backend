package pe.greenminds.ecomind.community.interfaces.acl.events;
import java.time.Instant;
import java.util.*;
public record CommunityEventCompletedIntegrationEvent(UUID eventId,UUID executionId,UUID communityId,List<Long> eligibleParticipantIds,CommunityGoalCompletedIntegrationEvent.ConfiguredReward configuredReward,Instant occurredAt) {
  public CommunityEventCompletedIntegrationEvent {
    Objects.requireNonNull(eventId);Objects.requireNonNull(executionId);Objects.requireNonNull(communityId);Objects.requireNonNull(occurredAt);eligibleParticipantIds=List.copyOf(eligibleParticipantIds);
    if(eligibleParticipantIds.stream().anyMatch(id->id==null || id<=0) || eligibleParticipantIds.stream().distinct().count()!=eligibleParticipantIds.size()) throw new IllegalArgumentException("Participants must be unique positive ids");
  }
}
