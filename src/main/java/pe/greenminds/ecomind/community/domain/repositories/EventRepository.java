package pe.greenminds.ecomind.community.domain.repositories;
import java.util.List;import java.util.Optional;import pe.greenminds.ecomind.community.domain.model.aggregates.Event;
public interface EventRepository{Event save(Event event);Optional<Event> findById(Long id);List<Event> findAll(Long communityId);void delete(Event event);}
