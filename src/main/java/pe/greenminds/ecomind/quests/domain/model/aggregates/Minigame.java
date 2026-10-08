package pe.greenminds.ecomind.quests.domain.model.aggregates;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Minigame extends AbstractDomainAggregateRoot<Minigame> {
    @Getter
    @Setter
    private Long id;

    private String name;
    private String description;
    private String url;
    private Map<String, Object> completionRules;

    public Minigame(
            Long id,
            String name,
            String description,
            String url,
            Map<String, Object> completionRules
    ) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = description;
        this.url = Objects.requireNonNull(url, "url must not be null");
        this.completionRules = copyMap(completionRules);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, Object> getCompletionRules() {
        return completionRules;
    }

    private static Map<String, Object> copyMap(Map<String, Object> map) {
        if (map == null) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }
}
