package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;
import pe.greenminds.ecomind.quests.domain.model.events.ActivityCreatedEvent;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;

import java.util.Map;
import java.util.Objects;

public class Activity extends AbstractDomainAggregateRoot<Activity> {

    @Getter
    @Setter
    private Long id;

    @Setter
    private Long questId;
    private String description;

    @Setter
    private Integer order;
    private ActivityType activityType;
    private Map<String, Object> activityConfiguration;
    private String image;

    public Activity() {

    }

    public Activity(
            Long id,
            Long questId,
            String description,
            Integer order,
            ActivityType activityType,
            Map<String, Object> activityConfiguration,
            String image
    ) {
        this.id = id;
        this.questId = Objects.requireNonNull(questId, "QuestID is required");
        this.description = description;
        this.order = Objects.requireNonNull(order, "Order is required");
        this.activityType = Objects.requireNonNull(activityType, "ActivityType is required");
        this.activityConfiguration = activityConfiguration;
        this.image = image;

        if(order < 1){
            throw new IllegalArgumentException("Order must be more or equal to 1");
        }
    }

    public Activity(
            Long questId,
            String description,
            Integer order,
            ActivityType type,
            Map<String, Object> activityConfiguration,
            String image
    ) {
        this(null, questId, description, order, type, activityConfiguration, image);
    }

    public void onCreated() {
    }

    public void update(
            String description,
            Integer order,
            ActivityType activityType,
            Map<String, Object> activityConfiguration,
            String image
    ) {
        this.description = description;
        this.order = Objects.requireNonNull(order, "Order is required");
        this.activityType = Objects.requireNonNull(activityType, "ActivityType is required");
        this.activityConfiguration = activityConfiguration;
        this.image = image;

        if (order < 1) {
            throw new IllegalArgumentException("Order must be more or equal to 1");
        }
    }

    public Long getQuestId() {
        return questId;
    }

    public String getDescription() {
        return description;
    }

    public Integer getOrder() {
        return order;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public Map<String, Object> getActivityConfiguration() {
        return activityConfiguration;
    }

    public String getImage() {
        return image;
    }

}
