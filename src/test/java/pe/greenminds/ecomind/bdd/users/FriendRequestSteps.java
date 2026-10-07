package pe.greenminds.ecomind.bdd.users;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import pe.greenminds.ecomind.bdd.ApiClient;

/**
 * Steps of HU-039, friend requests.
 */
public class FriendRequestSteps {

  private final UsersApiDriver users;
  private final ApiClient api;
  private final JdbcTemplate jdbcTemplate;

  // Friend request the scenario is working with: the last one sent.
  private Long friendshipId;

  public FriendRequestSteps(UsersApiDriver users, ApiClient api, JdbcTemplate jdbcTemplate) {
    this.users = users;
    this.api = api;
    this.jdbcTemplate = jdbcTemplate;
  }

  @When("{string} sends a friend request to {string}")
  public void sendsAFriendRequest(String name, String receiverName) {
    users.sendFriendRequest(name, receiverName);
    if (api.status() == 201) {
      friendshipId = Long.valueOf(api.jsonValue("$.id"));
    }
  }

  @Given("{string} has sent a friend request to {string}")
  public void hasSentAFriendRequest(String name, String receiverName) {
    sendsAFriendRequest(name, receiverName);
    assertThat(api.status()).as(api.body()).isEqualTo(201);
  }

  @When("{string} accepts the friend request")
  public void acceptsTheFriendRequest(String name) {
    users.respondFriendRequest(name, friendshipId, true);
  }

  @When("{string} rejects the friend request")
  public void rejectsTheFriendRequest(String name) {
    users.respondFriendRequest(name, friendshipId, false);
  }

  // Time cannot pass inside a test, so the date of the answer is moved to the past instead.
  @Given("{int} days have passed since the friend request was answered")
  public void daysHavePassedSinceTheAnswer(int days) {
    Instant answeredAt = Instant.now().minus(days, ChronoUnit.DAYS);
    jdbcTemplate.update(
        "UPDATE friendships SET updated_at = ? WHERE id = ?",
        Timestamp.from(answeredAt),
        friendshipId);
  }

  @Then("the friend request is {string}")
  public void theFriendRequestIs(String status) {
    assertThat(api.jsonValue("$.status")).isEqualTo(status);
  }

  @Then("{string} sees a {string} friend request from {string}")
  public void seesAFriendRequestFrom(String name, String status, String requesterName) {
    users.getFriendRequests(name);
    List<Object> matches =
        api.jsonObject(
            "$[?(@.requesterId == %d && @.status == '%s')]"
                .formatted(users.idOf(requesterName), status));
    assertThat(matches).hasSize(1);
  }
}
