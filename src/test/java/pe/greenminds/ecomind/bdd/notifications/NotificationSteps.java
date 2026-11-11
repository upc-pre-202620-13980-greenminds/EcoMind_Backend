package pe.greenminds.ecomind.bdd.notifications;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.bdd.users.UsersApiDriver;

/**
 * Steps of TS-016, the notification inbox. Friend requests are sent and accepted with the steps
 * of HU-039.
 */
public class NotificationSteps {

  private static final String NOTIFICATIONS = "/api/v1/notification";

  private final UsersApiDriver users;
  private final ApiClient api;

  // Notification the scenario is working with: the last one created
  private Long notificationId;

  public NotificationSteps(UsersApiDriver users, ApiClient api) {
    this.users = users;
    this.api = api;
  }

  @When("{string} lists their notifications")
  public void listsTheirNotifications(String name) {
    api.get(NOTIFICATIONS, users.tokenOf(name));
  }

  @Then("{string} has {int} notification(s) of type {string}")
  public void hasNotificationsOfType(String name, int count, String type) {
    api.get(NOTIFICATIONS, users.tokenOf(name));
    List<Object> matches = api.jsonObject("$[?(@.type == '%s')]".formatted(type));
    assertThat(matches).hasSize(count);
  }

  @Then("the first notification says {string}")
  public void theFirstNotificationSays(String message) {
    assertThat(api.jsonValue("$[0].message")).isEqualTo(message);
  }

  @Given("{string} creates a notification {string}")
  public void createsANotification(String name, String title) {
    api.post(
        NOTIFICATIONS,
        Map.of("category", "LEARNING_REMINDERS", "title", title, "message", "Review it today."),
        users.tokenOf(name));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    notificationId = Long.valueOf(api.jsonValue("$.id"));
  }

  @When("{string} marks the first notification as read")
  public void marksTheFirstNotificationAsRead(String name) {
    api.get(NOTIFICATIONS, users.tokenOf(name));
    notificationId = Long.valueOf(api.jsonValue("$[0].id"));
    api.patchWithoutBody(NOTIFICATIONS + "/" + notificationId + "/read", users.tokenOf(name));
  }

  @When("{string} marks the notification of {string} as read")
  public void marksTheNotificationOfAnotherUserAsRead(String name, String ownerName) {
    api.patchWithoutBody(NOTIFICATIONS + "/" + notificationId + "/read", users.tokenOf(name));
  }

  @When("{string} marks a notification that does not exist as read")
  public void marksANotificationThatDoesNotExistAsRead(String name) {
    api.patchWithoutBody(NOTIFICATIONS + "/999999/read", users.tokenOf(name));
  }

  @When("{string} marks all notifications as read")
  public void marksAllNotificationsAsRead(String name) {
    api.patchWithoutBody(NOTIFICATIONS + "/read-all", users.tokenOf(name));
  }

  @Then("{string} has {int} unread notification(s)")
  public void hasUnreadNotifications(String name, int count) {
    api.get(NOTIFICATIONS + "/unread-count", users.tokenOf(name));
    assertThat(api.status()).as(api.body()).isEqualTo(200);
    assertThat(api.jsonValue("$.count")).isEqualTo(String.valueOf(count));
  }
}
