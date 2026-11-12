package pe.greenminds.ecomind.community.infrastructure.scheduling;

import java.time.Instant;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.community.application.commandservices.EventLifecycleService;
import pe.greenminds.ecomind.community.domain.repositories.EventRepository;

@Component
@EnableScheduling
public class EventChatExpirationScheduler {
    private final EventRepository events;
    private final EventLifecycleService lifecycle;

    public EventChatExpirationScheduler(
            EventRepository events,
            EventLifecycleService lifecycle) {
        this.events = events;
        this.lifecycle = lifecycle;
    }

    @Scheduled(fixedDelayString = "${community.event-chat.cleanup-delay-ms:60000}")
    public void expireChats() {
        var now = Instant.now();

        for (var event : events.findAll(null)) {
            if (!event.isChatOpen(now)) {
                lifecycle.expireMessages(event.getId());
            }
        }
    }
}