package pe.greenminds.ecomind.quests.interfaces.acl.resources;

/** Configured base amounts; Gamification owns the final grant calculation. */
public record QuestRewardResource(long ecopoints, long experience, int gems) {
  public QuestRewardResource {
    if (ecopoints < 0 || experience < 0 || gems < 0)
      throw new IllegalArgumentException("Reward amounts cannot be negative");
  }
}
