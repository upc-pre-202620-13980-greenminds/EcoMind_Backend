package pe.greenminds.ecomind.notifications.domain.model.valueobjects;

/**
 * What produced the notification. A generated type keeps only its data and its texts are written
 * in the language of whoever reads it; a GENERAL one keeps the texts it was created with.
 */
public enum NotificationType {
  GENERAL,
  FRIEND_REQUEST_ACCEPTED,
  /** A collaborative quest invitation from Quests (NEW_AND_DAILY_QUESTS). */
  QUEST_INVITATION,
  /** An individual achievement from Gamification (ACHIEVEMENTS_AND_BADGES). */
  ACHIEVEMENT_UNLOCKED,
  /** A member shared an achievement in the community (COMMUNITY_ACTIVITY). */
  ACHIEVEMENT_SHARED,
  /** The community completed one of its goals (COMMUNITY_ACTIVITY). */
  COMMUNITY_GOAL_REACHED,
  /** A community event for families was published (COMMUNITY_EVENTS, HU-018). */
  COMMUNITY_EVENT_CREATED,
  /** A community event for families changed (COMMUNITY_EVENTS, HU-018). */
  COMMUNITY_EVENT_UPDATED,
  /** A favorite material that was not opened yet (LEARNING_REMINDERS, HU-025). */
  LEARNING_REMINDER,
  /** EcoPoints earned in the week and current streak (WEEKLY_PROGRESS_SUMMARY). */
  WEEKLY_SUMMARY
}
