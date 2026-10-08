package pe.greenminds.ecomind.gamification.application.internal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import pe.greenminds.ecomind.gamification.application.commandservices.StreakProtectionCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.RequestStreakProtectionCommand;
import pe.greenminds.ecomind.gamification.domain.repositories.UserProgressRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Component
public class DailyStreakClosureService {
    private final UserProgressRepository users;
    private final StreakProtectionCommandService commands;
    private final ZoneId zone;
    private final boolean enabled;

    public DailyStreakClosureService(
            UserProgressRepository users,
            StreakProtectionCommandService commands,
            @Value("${gamification.activity-zone:America/Lima}") String zone,
            @Value("${gamification.streak-closure.enabled:true}") boolean enabled) {
        this.users = users;
        this.commands = commands;
        this.zone = ZoneId.of(zone);
        this.enabled = enabled;
    }

    @Scheduled(fixedDelayString = "${gamification.streak-closure.delay-ms:60000}")
    public void closeMissingDays() {
        if (enabled) closeThrough(LocalDate.now(zone).minusDays(1), Instant.now());
    }

    public void closeThrough(LocalDate lastClosedDay, Instant at) {
        for (var id : users.findActiveStreakUsers()) {
            var user = users.findByUserId(id).orElseThrow();
            var last = user.getLastContinuityDate();
            if (last != null && last.isBefore(lastClosedDay))
                commands.handle(new RequestStreakProtectionCommand(id, last.plusDays(1), at));
        }
    }
}
