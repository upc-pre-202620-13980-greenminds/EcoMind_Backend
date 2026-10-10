package pe.greenminds.ecomind.bdd.quests;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.bdd.users.UsersApiDriver;

public class QuestSteps {
  private final ApiClient api;
  private final UsersApiDriver users;
  private final JdbcTemplate jdbc;
  private Long questId, assignmentId;

  public QuestSteps(ApiClient api, UsersApiDriver users, JdbcTemplate jdbc) {
    this.api = api;
    this.users = users;
    this.jdbc = jdbc;
  }

  @Before(value = "@quests", order = -1)
  @After("@quests")
  public void cleanQuests() {
    for (String table : List.of("activity_users", "user_quests", "activities", "quests"))
      jdbc.update("DELETE FROM " + table);
  }

  @Given("a draft environmental quest created by {string}")
  public void draft(String actor) {
    api.post(
        "/api/v1/quests",
        Map.of(
            "title",
            "Switch off unused lights",
            "description",
            "Check each room",
            "category",
            "ENERGY",
            "type",
            "ACTIVITIES",
            "gemReward",
            2,
            "ecopoints",
            10,
            "age",
            8,
            "time",
            2,
            "theme",
            "CHECKBOX"),
        users.tokenOf(actor));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    questId = Long.valueOf(api.jsonValue("$.id"));
    api.post(
        "/api/v1/activities",
        Map.of(
            "questId",
            questId,
            "description",
            "Check the living room",
            "order",
            1,
            "type",
            "CHECKBOX"),
        users.tokenOf(actor));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
  }

  @When("{string} publishes the environmental quest")
  public void publish(String actor) {
    api.patchWithoutBody("/api/v1/quests/" + questId + "/publish", users.tokenOf(actor));
  }

  @When("{string} starts the environmental quest")
  public void start(String actor) {
    api.post("/api/v1/quest-users", Map.of("questId", questId), users.tokenOf(actor));
    if (api.status() == 201) assignmentId = Long.valueOf(api.jsonValue("$.id"));
  }

  @When("{string} completes the quest assignment")
  public void complete(String actor) {
    api.postWithoutBody("/api/v1/quest-users/" + assignmentId + "/complete", users.tokenOf(actor));
  }

  @When("{string} checks the assigned activity")
  public void check(String actor) {
    api.get("/api/v1/activity-users/quest-user/" + assignmentId, users.tokenOf(actor));
    assertThat(api.status()).isEqualTo(200);
    String activity = api.jsonValue("$[0].id");
    api.post(
        "/api/v1/activity-users/" + activity + "/submit",
        Map.of("data", Map.of("checked", true)),
        users.tokenOf(actor));
  }

  @When("{string} cancels the quest assignment")
  public void cancel(String actor) {
    api.patchWithoutBody("/api/v1/quest-users/" + assignmentId + "/cancel", users.tokenOf(actor));
  }

  @When("{string} archives the environmental quest")
  public void archive(String actor) {
    api.patchWithoutBody("/api/v1/quests/" + questId + "/archive", users.tokenOf(actor));
  }

  @When("{string} reads the published quest catalog")
  public void catalog(String actor) {
    api.get("/api/v1/quests", users.tokenOf(actor));
  }

  @Then("{string} has earned {int} ecopoints from the quest")
  public void points(String actor, int amount) {
    api.get("/api/v1/gamification/me/progress", users.tokenOf(actor));
    assertThat(api.status()).isEqualTo(200);
    assertThat(api.jsonValue("$.totalEcopoints")).isEqualTo(Integer.toString(amount));
    api.get("/api/v1/gamification/me/rewards", users.tokenOf(actor));
    assertThat(api.status()).isEqualTo(200);
    java.util.List<?> rewards = api.jsonObject("$");
    assertThat(rewards).hasSize(1);
  }

  @When("{string} attempts to {string} another user's quest assignment")
  public void otherUser(String actor, String operation) {
    String token = users.tokenOf(actor);
    switch (operation) {
      case "read" -> api.get("/api/v1/quest-users/" + assignmentId, token);
      case "read version" ->
          api.get("/api/v1/quest-users/" + assignmentId + "/version-status", token);
      case "cancel" ->
          api.patchWithoutBody("/api/v1/quest-users/" + assignmentId + "/cancel", token);
      case "complete" ->
          api.postWithoutBody("/api/v1/quest-users/" + assignmentId + "/complete", token);
      case "list activities" -> api.get("/api/v1/activity-users/quest-user/" + assignmentId, token);
      case "submit activity", "read activity", "assign activity" -> {
        api.get("/api/v1/activity-users/quest-user/" + assignmentId, users.tokenOf("Bruno"));
        String activityUserId = api.jsonValue("$[0].id");
        Long activityId = Long.valueOf(api.jsonValue("$[0].activityId"));
        switch (operation) {
          case "submit activity" ->
              api.post(
                  "/api/v1/activity-users/" + activityUserId + "/submit",
                  Map.of("data", Map.of("checked", true)),
                  token);
          case "read activity" -> api.get("/api/v1/activity-users/" + activityUserId, token);
          default ->
              api.post(
                  "/api/v1/activity-users",
                  Map.of("questUserId", assignmentId, "activityId", activityId),
                  token);
        }
      }
      default -> throw new IllegalArgumentException(operation);
    }
  }

  @Then("{string} still has the quest in progress")
  public void unchanged(String actor) {
    api.get("/api/v1/quest-users/" + assignmentId, users.tokenOf(actor));
    assertThat(api.status()).isEqualTo(200);
    assertThat(api.jsonValue("$.status")).isEqualTo("IN_PROGRESS");
    assertThat(api.jsonValue("$.progress")).isEqualTo("0.0");
  }

  @Then("the environmental quest is absent from the public catalog")
  public void absent() {
    List<Number> ids = api.jsonObject("$[*].id");
    assertThat(ids.stream().map(Number::longValue)).doesNotContain(questId);
  }

  @Then("the quest assignment belongs to {string}")
  public void owner(String actor) {
    assertThat(api.jsonValue("$.userId")).isEqualTo(users.idOf(actor).toString());
  }

  @Then("the quest assignment status is {string}")
  public void status(String status) {
    assertThat(api.jsonValue("$.status")).isEqualTo(status);
  }
}
