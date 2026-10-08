package pe.greenminds.ecomind.community.interfaces.rest.transform;
import pe.greenminds.ecomind.community.domain.model.aggregates.Event;import pe.greenminds.ecomind.community.interfaces.rest.resources.EventResource;
public final class EventResourceFromEntityAssembler{private EventResourceFromEntityAssembler(){}public static EventResource toResourceFromEntity(Event e){return new EventResource(e.getId(),e.getCommunityId(),e.getAuthorId(),e.getName(),e.getDescription(),e.getDate(),e.getStartTime(),e.getLocation(),e.getLatitude(),e.getLongitude(),e.getCapacity(),e.getImageUrl());}}
