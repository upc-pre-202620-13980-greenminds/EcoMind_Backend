package pe.greenminds.ecomind.bdd.users;

import io.cucumber.spring.ScenarioScope;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.bdd.iam.IamApiDriver;

/**
 * Registers the users of a scenario through IAM and calls the Users endpoints on their behalf.
 * Users are referred to by their first name in the feature files.
 */
@Component
@ScenarioScope
public class UsersApiDriver {

  private static final String PASSWORD = "GreenPlanet2026";

  private record RegisteredUser(Long id, String accessToken) {
  }

  private final ApiClient api;
  private final IamApiDriver iam;
  private final Map<String, RegisteredUser> users = new HashMap<>();

  public UsersApiDriver(ApiClient api, IamApiDriver iam) {
    this.api = api;
    this.iam = iam;
  }

  /** Registers the account, which also creates the profile, and signs the user in. */
  public void register(String name, String socialRole) {
    String email = name.toLowerCase(Locale.ROOT) + "@example.com";
    Long id = iam.registerAccount(name, email, PASSWORD, socialRole);
    users.put(name, new RegisteredUser(id, iam.signIn(email, PASSWORD)));
  }

  public Long idOf(String name) {
    return user(name).id();
  }

  public void createFamily(String name, String familyName, String commitment) {
    api.post(
        "/api/v1/family",
        Map.of("name", familyName, "commitment", commitment),
        tokenOf(name));
  }

  public void addFamilyMember(String name, Long familyId, String memberName, String familyRole) {
    api.post(
        "/api/v1/family_user",
        Map.of("familyId", familyId, "userId", idOf(memberName), "familyRole", familyRole),
        tokenOf(name));
  }

  public void removeFamilyMember(String name, Long familyMemberId) {
    api.delete("/api/v1/family_user/" + familyMemberId, tokenOf(name));
  }

  public void getFamilyMembership(String name, String memberName) {
    api.get("/api/v1/family_user?user_id=" + idOf(memberName), tokenOf(name));
  }

  public void sendFriendRequest(String name, String receiverName) {
    api.post("/api/v1/friend", Map.of("receiverId", idOf(receiverName)), tokenOf(name));
  }

  public void respondFriendRequest(String name, Long friendshipId, boolean accepted) {
    api.put("/api/v1/friend/" + friendshipId, Map.of("accepted", accepted), tokenOf(name));
  }

  public void getFriendRequests(String name) {
    api.get("/api/v1/friend?user_id=" + idOf(name), tokenOf(name));
  }

  public String tokenOf(String name) {
    return user(name).accessToken();
  }

  private RegisteredUser user(String name) {
    RegisteredUser user = users.get(name);
    if (user == null) {
      throw new IllegalStateException("The user " + name + " is not registered in this scenario");
    }
    return user;
  }
}
