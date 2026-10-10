package pe.greenminds.ecomind.bdd.quests;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.bdd.users.UsersApiDriver;

public class CollaborativeQuestSteps {
  private final ApiClient api;
  private final UsersApiDriver users;
  private final JdbcTemplate jdbc;
  private long quest, session, invitation;

  public CollaborativeQuestSteps(ApiClient api, UsersApiDriver users, JdbcTemplate jdbc) {
    this.api = api;
    this.users = users;
    this.jdbc = jdbc;
  }

  @Before(value = "@quest_lifecycle", order = -2)
  @After("@quest_lifecycle")
  public void clearQuests() {
    for (String table :
        List.of(
            "activity_users",
            "user_quests",
            "family_plan_items",
            "family_plans",
            "collaborative_quest_members",
            "collaborative_quest_sessions",
            "activities",
            "quests")) jdbc.update("DELETE FROM " + table);
  }

  @Given("{string} and {string} share a family for collaborative quests")
  public void family(String parent, String child) {
    users.createFamily(parent, "Shared family", "Save together");
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    long family = Long.parseLong(api.jsonValue("$.id"));
    users.addFamilyMember(parent, family, child, "CHILD");
    assertThat(api.status()).as(api.body()).isEqualTo(201);
  }

  @Given("{string} has a published collaborative quest session")
  public void session(String owner) {
    api.post(
        "/api/v1/quests",
        Map.of(
            "title",
            "Shared energy audit",
            "description",
            "Check rooms",
            "category",
            "ENERGY",
            "type",
            "COLLABORATIVE",
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
        users.tokenOf(owner));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    quest = Long.parseLong(api.jsonValue("$.id"));
    api.post(
        "/api/v1/activities",
        Map.of("questId", quest, "description", "Check the room", "order", 1, "type", "CHECKBOX"),
        users.tokenOf(owner));
    assertThat(api.status()).isEqualTo(201);
    api.patchWithoutBody("/api/v1/quests/" + quest + "/publish", users.tokenOf(owner));
    assertThat(api.status()).isEqualTo(200);
    api.post("/api/v1/collaborative-quests", Map.of("questId", quest), users.tokenOf(owner));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    session = Long.parseLong(api.jsonValue("$.id"));
  }

  @Given("{string} invites {string} to the collaborative session")
  public void invite(String owner, String participant) {
    api.post(
        "/api/v1/collaborative-quest-members",
        Map.of("sessionId", session, "invitedUserId", users.idOf(participant)),
        users.tokenOf(owner));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    invitation = Long.parseLong(api.jsonValue("$.id"));
  }

  @When("{string} attempts to {string} the collaborative invitation")
  public void answer(String actor, String action) {
    api.patchWithoutBody(
        "/api/v1/collaborative-quest-members/" + invitation + "/" + action, users.tokenOf(actor));
  }

  @When("{string} starts the shared session")
  public void start(String actor) {
    api.postWithoutBody("/api/v1/collaborative-quests/" + session + "/start", users.tokenOf(actor));
  }

  @Then("the collaborative invitation remains pending for {string}")
  public void pending(String actor) {
    status(actor, "PENDING");
  }

  @Then("the collaborative invitation is rejected for {string}")
  public void rejected(String actor) {
    status(actor, "REJECTED");
  }

  private void status(String actor, String expected) {
    api.get("/api/v1/collaborative-quest-members/" + invitation, users.tokenOf(actor));
    assertThat(api.status()).isEqualTo(200);
    assertThat(api.jsonValue("$.status")).isEqualTo(expected);
  }

  @When("{string} completes the shared activity")
  public void submit(String actor) {
    api.get("/api/v1/quest-users/me/quest/" + quest, users.tokenOf(actor));
    String assignment = api.jsonValue("$.id");
    api.get("/api/v1/activity-users/quest-user/" + assignment, users.tokenOf(actor));
    String activity = api.jsonValue("$[0].id");
    api.post(
        "/api/v1/activity-users/" + activity + "/submit",
        Map.of("data", Map.of("checked", true)),
        users.tokenOf(actor));
    assertThat(api.status()).as(api.body()).isEqualTo(200);
  }

  @Then("both collaborative participants are ready to complete")
  public void ready() {
    for (String actor : List.of("Rosa", "Luis")) {
      api.get("/api/v1/quest-users/me/quest/" + quest, users.tokenOf(actor));
      assertThat(api.status()).isEqualTo(200);
      assertThat(api.jsonValue("$.status")).isEqualTo("READY_TO_COMPLETE");
    }
  }
}
