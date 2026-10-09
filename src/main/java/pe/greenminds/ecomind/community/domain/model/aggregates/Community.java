package pe.greenminds.ecomind.community.domain.model.aggregates;

import java.util.Objects;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;

public class Community {
  private final Long id;
  private final String name;
  private final String description;
  private final CommunityType type;
  private final String topic;
  private final String locality;
  private final Integer memberLimit;
  private final String iconUrl;
  private final Long createdBy;

  public Community(Long id, String name, String description, CommunityType type, String topic,
      String locality, Integer memberLimit, String iconUrl,
      Long createdBy) {
    if (name == null || name.isBlank()) throw new IllegalArgumentException("Community name is required");
    Objects.requireNonNull(type, "Community type is required");
    if (type == CommunityType.LOCAL && (locality == null || locality.isBlank())) throw new IllegalArgumentException("Locality is required for local communities");
    if (type == CommunityType.TOPIC && (topic == null || topic.isBlank())) throw new IllegalArgumentException("Topic is required for topic communities");
    if (type == CommunityType.TOPIC && createdBy == null) throw new IllegalArgumentException("Topic communities require a creator");
    if (memberLimit != null && memberLimit < 1) throw new IllegalArgumentException("Member limit must be positive");
    this.id=id; this.name=name.trim(); this.description=description; this.type=type; this.topic=topic;
    this.locality=locality; this.memberLimit=memberLimit;
    this.iconUrl=iconUrl; this.createdBy=createdBy;
  }
  public Long getId(){return id;}
  public String getName(){return name;}
  public String getDescription(){return description;}
  public CommunityType getType(){return type;}
  public String getTopic(){return topic;}
  public String getLocality(){return locality;}
  public Integer getMemberLimit(){return memberLimit;}
  public String getIconUrl(){return iconUrl;}
  public Long getCreatedBy(){return createdBy;}
}
