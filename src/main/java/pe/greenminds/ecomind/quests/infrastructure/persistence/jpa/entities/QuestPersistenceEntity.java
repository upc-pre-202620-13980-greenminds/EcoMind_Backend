package pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.entities;
import jakarta.persistence.*;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.infrastructure.persistence.jpa.embedddables.RewardPersistenceEmbeddable;
import pe.greenminds.ecomind.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;

import java.time.LocalDate;

@Entity
@Table(name = "quests")
public class QuestPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "minigame_id")
    private Long minigameId;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="description",  nullable=false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="category")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=30, name ="type")
    private QuestType questType;

    @Column(name="age")
    private Integer age;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "gemReward", column = @Column(name = "gem_reward")),
            @AttributeOverride(name = "ecopointsReward", column = @Column(name = "points_reward"))})
    private RewardPersistenceEmbeddable reward;

    @Column(name="time")
    private Integer time;

    @Column(name = "image_url", length = 255)
    private String image;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="theme")
    private Theme theme;

    @Column(name="assignedDate")
    private LocalDate assignedDate;

    public RewardPersistenceEmbeddable getReward() {
        return reward;
    }

    public Long getMinigameId() {
        return minigameId;
    }

    public QuestType getQuestType() {
        return questType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public Integer getAge() {
        return age;
    }

    public Integer getTime() {
        return time;
    }

    public String getImage() {
        return image;
    }
    public Theme getTheme() {
        return theme;
    }
    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setMinigameId(Long minigameId) {
        this.minigameId = minigameId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setQuestType(QuestType questType) {
        this.questType = questType;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setReward(RewardPersistenceEmbeddable reward) {
        this.reward = reward;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setTheme(Theme theme) {
        this.theme = theme;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

}
