package pe.greenminds.ecomind.community.domain.model.events;
public record EventCreatedEvent(Long eventId,Long communityId,Long authorId,String name){}
