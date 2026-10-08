package pe.greenminds.ecomind.gamification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pe.greenminds.ecomind.gamification.domain.model.commands.ConfigureQuestExperienceCommand;
import pe.greenminds.ecomind.gamification.domain.repositories.QuestExperienceRepository;

/** Trusted catalog configuration; mobile users cannot set their reward amounts. */
@Service
public class QuestExperienceCommandService {
    private final QuestExperienceRepository experiences;

    public QuestExperienceCommandService(QuestExperienceRepository experiences) {
        this.experiences = experiences;
    }

    @Transactional
    public void handle(ConfigureQuestExperienceCommand c) {
        experiences.configure(c.questId(), c.experience());
    }
}
