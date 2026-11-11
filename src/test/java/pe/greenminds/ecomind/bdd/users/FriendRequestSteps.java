package pe.greenminds.ecomind.bdd.users;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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

  // Invitation code a user shared with the requester in the scenario.
  private String sharedInvitationCode;

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

  // Same idea for the sending date, which decides when a pending request expires.
  @Given("{int} hours have passed since the friend request was sent")
  public void hoursHavePassedSinceTheRequestWasSent(int hours) {
    Instant sentAt = Instant.now().minus(hours, ChronoUnit.HOURS);
    jdbcTemplate.update(
        "UPDATE friendships SET created_at = ? WHERE id = ?", Timestamp.from(sentAt), friendshipId);
  }

  @When("{string} looks at the profile of {string}")
  public void looksAtTheProfileOf(String name, String ownerName) {
    users.getProfile(name, ownerName);
    assertThat(api.status()).as(api.body()).isEqualTo(200);
  }

  @Then("the invitation code is shown")
  public void theInvitationCodeIsShown() {
    assertThat(api.jsonValue("$.invitationCode")).matches("^[A-HJ-NP-Z2-9]{8}$");
  }

  @Then("the invitation code is hidden")
  public void theInvitationCodeIsHidden() {
    assertThat(api.jsonValue("$.invitationCode")).isNull();
  }

  @Given("{string} shares the invitation code of {string}")
  public void sharesTheInvitationCode(String ownerName, String sameOwnerName) {
    users.getProfile(ownerName, sameOwnerName);
    sharedInvitationCode = api.jsonValue("$.invitationCode");
    assertThat(sharedInvitationCode).isNotNull();
  }

  @When("{string} sends a friend request with the shared invitation code")
  public void sendsAFriendRequestWithTheSharedCode(String name) {
    sendWithBody(name, Map.of("invitationCode", sharedInvitationCode));
  }

  @When("{string} sends a friend request with the invitation code {string}")
  public void sendsAFriendRequestWithTheCode(String name, String invitationCode) {
    sendWithBody(name, Map.of("invitationCode", invitationCode));
  }

  @When("{string} sends a friend request without a receiver")
  public void sendsAFriendRequestWithoutAReceiver(String name) {
    sendWithBody(name, Map.of());
  }

  private void sendWithBody(String name, Map<String, Object> body) {
    users.sendFriendRequestWithBody(name, body);
    if (api.status() == 201) {
      friendshipId = Long.valueOf(api.jsonValue("$.id"));
    }
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
