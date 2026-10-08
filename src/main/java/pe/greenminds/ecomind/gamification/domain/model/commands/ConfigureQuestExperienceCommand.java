package pe.greenminds.ecomind.gamification.domain.model.commands;

public record ConfigureQuestExperienceCommand(Long questId, long experience) {
    public ConfigureQuestExperienceCommand {
        if (questId == null || questId <= 0 || experience < 0)
            throw new IllegalArgumentException("Invalid quest experience configuration");
    }
}
