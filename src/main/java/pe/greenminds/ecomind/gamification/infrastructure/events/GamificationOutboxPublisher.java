package pe.greenminds.ecomind.gamification.infrastructure.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.infrastructure.persistence.jpa.repositories.GamificationOutboxPersistenceRepository;

import java.time.Instant;

@Component
@EnableScheduling
public class GamificationOutboxPublisher {
    private static final Logger LOG = LoggerFactory.getLogger(GamificationOutboxPublisher.class);
    private final GamificationOutboxPersistenceRepository messages;
    private final GamificationOutboxDeliveryService delivery;
    private final boolean enabled;

    public GamificationOutboxPublisher(
            GamificationOutboxPersistenceRepository messages,
            GamificationOutboxDeliveryService delivery,
            @Value("${gamification.outbox.enabled:true}") boolean enabled) {
        this.messages = messages;
        this.delivery = delivery;
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${gamification.outbox.delay-ms:5000}")
    public void publishPending() {
        if (!enabled) return;
        for (var row :
                messages.findTop50ByDeliveredAtIsNullAndNextAttemptAtLessThanEqualOrderByOccurredAt(
                        Instant.now())) {
            try {
                delivery.deliver(row.getId());
            } catch (RuntimeException failure) {
                delivery.retryLater(row.getId());
                LOG.warn(
                        "Gamification message {} remains pending ({})",
                        row.getId(),
                        failure.getClass().getSimpleName());
            }
        }
    }
}
