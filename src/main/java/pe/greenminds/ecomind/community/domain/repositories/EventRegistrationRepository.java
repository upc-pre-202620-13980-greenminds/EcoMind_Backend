package pe.greenminds.ecomind.community.domain.repositories;
import java.util.List;import java.util.Optional;import pe.greenminds.ecomind.community.domain.model.aggregates.EventRegistration;
public interface EventRegistrationRepository{EventRegistration save(EventRegistration registration);Optional<EventRegistration> findByEventIdAndUserId(Long eventId,Long userId);Optional<EventRegistration> findById(Long id);List<EventRegistration> findByEventId(Long eventId);}
