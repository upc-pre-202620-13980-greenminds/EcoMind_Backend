package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.util.Objects;

public class Community {
  private final Long id;
  private final String name;
  private final String description;
  private final String type;
  private final String topic;
  private final String locality;
  private final Integer memberLimit;
  private final String iconUrl;
  private final Long createdBy;

  public Community(Long id, String name, String description, String type, String topic,
      String locality, Integer memberLimit, String iconUrl,
      Long createdBy) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("Community name is required");
    if (!"local".equals(type) && !"topic".equals(type)) throw new IllegalArgumentException("Type must be local or topic");
    if ("local".equals(type) && (locality == null || locality.isBlank())) throw new IllegalArgumentException("Locality is required for local communities");
    if ("topic".equals(type) && (topic == null || topic.isBlank())) throw new IllegalArgumentException("Topic is required for topic communities");
    if (memberLimit != null && memberLimit < 1) throw new IllegalArgumentException("Member limit must be positive");
    this.id=id; this.name=name.trim(); this.description=description; this.type=type; this.topic=topic;
    this.locality=locality; this.memberLimit=memberLimit;
    this.iconUrl=iconUrl; this.createdBy=Objects.requireNonNull(createdBy);
  }
  public Long getId(){return id;} public String getName(){return name;} public String getDescription(){return description;}
  public String getType(){return type;} public String getTopic(){return topic;} public String getLocality(){return locality;}
  public Integer getMemberLimit(){return memberLimit;}
  public String getIconUrl(){return iconUrl;} public Long getCreatedBy(){return createdBy;}
}
