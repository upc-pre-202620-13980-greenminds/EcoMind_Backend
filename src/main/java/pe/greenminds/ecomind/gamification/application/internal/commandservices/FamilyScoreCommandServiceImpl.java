package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.application.commandservices.FamilyScoreCommandService;
import pe.greenminds.ecomind.gamification.application.outboundservices.GamificationEventPublisher;
import pe.greenminds.ecomind.gamification.domain.model.commands.UpdateFamilyScoreCommand;
import pe.greenminds.ecomind.gamification.domain.model.events.FamilyScoreUpdatedEvent;
import pe.greenminds.ecomind.gamification.domain.repositories.FamilyScoreRepository;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class FamilyScoreCommandServiceImpl implements FamilyScoreCommandService {
    private final FamilyScoreRepository families;
    private final GamificationEventPublisher events;

    public FamilyScoreCommandServiceImpl(
            FamilyScoreRepository families, GamificationEventPublisher events) {
        this.families = families;
        this.events = events;
    }

    public void handle(UpdateFamilyScoreCommand command) {
        var score = families.lockForReward(command.familyId());
        score.addReward(command.ecopoints());
        families.save(score);
        events.publish(
                new FamilyScoreUpdatedEvent(
                        command.familyId(), command.executionId(), command.occurredAt()));
    }
}
