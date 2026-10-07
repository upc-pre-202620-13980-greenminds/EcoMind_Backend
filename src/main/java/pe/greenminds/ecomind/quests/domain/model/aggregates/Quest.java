package pe.greenminds.ecomind.quests.domain.model.aggregates;
import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.events.QuestCreatedEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;

import java.time.LocalDate;
import java.util.Objects;

public class Quest extends AbstractDomainAggregateRoot<Quest> {

    @Getter
    @Setter
    private Long id;

    @Getter
    private Reward reward;
    private Long minigameId;
    private String title;
    private String description;
    private Category category;
    private QuestType type;
    private Integer age;
    private Integer time;
    private String image;
    private Theme theme;
    private LocalDate assignedDate;


    public Quest(Long id, Long minigame_id, String title, Category category,
                 String description, QuestType type, Integer age,Reward reward, Integer time, String image, Theme theme, LocalDate assignedDate) {
        this.id = id;
        this.minigameId = minigame_id;
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.reward = Objects.requireNonNull(reward, "reward must not be null");
        this.time = time;
        this.image = image;
        this.age = age;
        this.theme = Objects.requireNonNull(theme, "theme must not be null");
        this.assignedDate = assignedDate;
    }

    public Quest(Long minigame_id, String title, Category category,
                 String description, QuestType type, Integer age,Reward reward, Integer time, String image,  Theme theme, LocalDate assignedDate) {

        this(null, minigame_id, title, category, description, type, age, reward, time, image, theme,  assignedDate);
    }

    public Quest(Long minigame_id, String title, Category category,
                 String description, QuestType type, Integer age, Integer gems, Integer ecopoints, Integer time, String image, Theme theme, LocalDate assignedDate) {

        this(null, minigame_id, title, category, description, type, age, new Reward(gems, ecopoints), time, image, theme,  assignedDate);
    }

    public Integer getGems() {return reward.gems();}
    public Integer getEcopoints() {return reward.ecopoints();}

    public Integer getTime() {return time;}
    public String getTitle() {return title;}
    public String getDescription() {return description;}
    public Category getCategory() {return category;}
    public QuestType getType() {return type;}
    public Integer getAge() {return age;}
    public Long getMinigameId() {return minigameId;}
    public String getImage() {return image;}
    public Theme getTheme() {return theme;}
    public LocalDate getAssignedDate() {return assignedDate;}

    public Reward getRewardValue() {
        return reward;
    }

    public void onCreated() {
        registerDomainEvent(QuestCreatedEvent.from(this));
    }

    public void update(
            Long minigameId,
            String title,
            Category category,
            String description,
            QuestType type,
            Integer age,
            Reward reward,
            Integer time,
            String image,
            Theme theme,
            LocalDate assignedDate
    ) {
        this.minigameId = minigameId;
        this.title = Objects.requireNonNull(title, "title must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.type = Objects.requireNonNull(type, "type must not be null");
        this.age = age;
        this.reward = Objects.requireNonNull(reward, "reward must not be null");
        this.time = time;
        this.image = image;
        this.theme = Objects.requireNonNull(theme, "theme must not be null");
        this.assignedDate = assignedDate;
    }
}
