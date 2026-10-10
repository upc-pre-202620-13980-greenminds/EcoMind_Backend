package pe.greenminds.ecomind.bdd.community;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.bdd.users.UsersApiDriver;

public class CommunitySteps {
  private static final String BASE = "/api/v1/Community";
  private final ApiClient api;
  private final UsersApiDriver users;
  private final JdbcTemplate jdbc;
  private Long communityId, postId, eventId, registrationId, goalId;

  public CommunitySteps(ApiClient api, UsersApiDriver users, JdbcTemplate jdbc) {
    this.api = api;
    this.users = users;
    this.jdbc = jdbc;
  }

  @Before(value = "@community", order = -1)
  @After("@community")
  public void cleanCommunity() {
    for (String table :
        List.of(
            "community_achievement_posts",
            "community_achievement_notices",
            "community_post_reactions",
            "community_posts",
            "community_event_registrations",
            "community_events",
            "community_achievements",
            "community_goals",
            "community_memberships",
            "community_communities")) {
      jdbc.update("DELETE FROM " + table);
    }
  }

  @Given("a local community managed by {string}")
  public void local(String actor) {
    api.post(
        BASE + "/Communities/Local",
        Map.of("name", "Green neighbors", "locality", "Lima"),
        users.tokenOf(actor));
    assertThat(api.status()).as(api.body()).isEqualTo(201);
    communityId = id();
  }

  @When("{string} joins the community")
  public void join(String actor) {
    api.postWithoutBody(
        BASE + "/Communities/" + communityId + "/Memberships", users.tokenOf(actor));
  }

  @When("{string} creates a topic community with {int} places")
  public void topic(String actor, int capacity) {
    api.post(
        BASE + "/Communities/Topics",
        Map.of("name", "Gardeners", "topic", "Gardening", "member_limit", capacity),
        users.tokenOf(actor));
    if (api.status() == 201) communityId = id();
  }

  @When("{string} publishes {string} in the community")
  public void publish(String actor, String content) {
    api.post(
        BASE + "/Posts",
        Map.of("community_id", communityId, "content", content),
        users.tokenOf(actor));
    if (api.status() == 201) postId = id();
  }

  @When("{string} deletes the publication")
  public void deletePost(String actor) {
    api.delete(BASE + "/Posts/" + postId, users.tokenOf(actor));
  }

  @Then("the publication belongs to {string}")
  public void author(String actor) {
    assertThat(api.jsonValue("$.author_id")).isEqualTo(users.idOf(actor).toString());
  }

  @When("{string} creates an event with {int} places")
  public void event(String actor, int places) {
    api.post(
        BASE + "/Events",
        Map.of(
            "community_id",
            communityId,
            "name",
            "Park cleanup",
            "date",
            "2026-12-01",
            "start_time",
            "10:00:00",
            "capacity",
            places),
        users.tokenOf(actor));
    if (api.status() == 201) eventId = id();
  }

  @When("{string} registers for the event individually")
  public void register(String actor) {
    api.post(
        BASE + "/Events/" + eventId + "/Registrations",
        Map.of("registration_type", "INDIVIDUAL"),
        users.tokenOf(actor));
    if (api.status() == 201) registrationId = id();
  }

  @When("{string} cancels the event registration")
  public void cancel(String actor) {
    api.patchWithoutBody(
        BASE + "/Events/" + eventId + "/Registrations/" + registrationId + "/cancel",
        users.tokenOf(actor));
  }

  @When("{string} creates a goal of {int} energy quests")
  public void goal(String actor, int target) {
    api.post(
        BASE + "/Community-goals",
        Map.of("community_id", communityId, "topic", "energy", "target", target),
        users.tokenOf(actor));
    if (api.status() == 201) goalId = id();
  }

  @When("{string} contributes to the community goal")
  public void contribute(String actor) {
    api.patchWithoutBody(BASE + "/Community-goals/" + goalId + "/progress", users.tokenOf(actor));
  }

  @Then("the community goal is completed")
  public void completed() {
    assertThat(api.jsonValue("$.status")).isEqualToIgnoringCase("completed");
  }

  @Then("the community has exactly {int} goal achievement")
  public void achievements(int count) {
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM community_achievements WHERE community_id=?",
                Integer.class,
                communityId))
        .isEqualTo(count);
  }

  @When("{string} reacts to the publication with {string}")
  public void react(String actor, String reaction) {
    api.post(
        BASE + "/Post-Reactions",
        Map.of("post_id", postId, "reaction_type", reaction),
        users.tokenOf(actor));
  }

  @When("{string} changes the publication reaction to {string}")
  public void changeReaction(String actor, String reaction) {
    api.patch(
        BASE + "/Posts/" + postId + "/Reactions",
        Map.of("reaction_type", reaction),
        users.tokenOf(actor));
  }

  @When("{string} removes the publication reaction")
  public void removeReaction(String actor) {
    api.delete(BASE + "/Posts/" + postId + "/Reactions", users.tokenOf(actor));
  }

  @Then("the publication has {int} reactions")
  public void reactionCount(int count) {
    assertThat(
            jdbc.queryForObject(
                "SELECT COUNT(*) FROM community_post_reactions WHERE post_id=?",
                Integer.class,
                postId))
        .isEqualTo(count);
  }

  private Long id() {
    return Long.valueOf(api.jsonValue("$.id"));
  }
}
