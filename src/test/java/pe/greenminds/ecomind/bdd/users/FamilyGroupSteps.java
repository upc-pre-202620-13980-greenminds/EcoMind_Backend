package pe.greenminds.ecomind.bdd.users;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.List;
import pe.greenminds.ecomind.bdd.ApiClient;

/**
 * Steps of HU-019, family group.
 */
public class FamilyGroupSteps {

  private static final String DEFAULT_COMMITMENT = "We will take care of the planet together";

  private final UsersApiDriver users;
  private final ApiClient api;

  // Family the scenario is working with: the last one created.
  private Long familyId;

  public FamilyGroupSteps(UsersApiDriver users, ApiClient api) {
    this.users = users;
    this.api = api;
  }

  @When("{string} creates the family {string} with the commitment {string}")
  public void createsTheFamily(String name, String familyName, String commitment) {
    users.createFamily(name, familyName, commitment);
    if (api.status() == 201) {
      familyId = Long.valueOf(api.jsonValue("$.id"));
    }
  }

  @Given("{string} has created the family {string}")
  public void hasCreatedTheFamily(String name, String familyName) {
    createsTheFamily(name, familyName, DEFAULT_COMMITMENT);
    assertThat(api.status()).as(api.body()).isEqualTo(201);
  }

  @When("{string} adds {string} to the family with role {string}")
  public void addsToTheFamily(String name, String memberName, String familyRole) {
    users.addFamilyMember(name, familyId, memberName, familyRole);
  }

  @Given("{string} has added {string} to the family with role {string}")
  public void hasAddedToTheFamily(String name, String memberName, String familyRole) {
    addsToTheFamily(name, memberName, familyRole);
    assertThat(api.status()).as(api.body()).isEqualTo(201);
  }

  @When("{string} removes {string} from the family")
  public void removesFromTheFamily(String name, String memberName) {
    users.getFamilyMembership(name, memberName);
    Long familyMemberId = Long.valueOf(api.jsonValue("$[0].id"));
    users.removeFamilyMember(name, familyMemberId);
  }

  @Then("the family has {int} member(s)")
  public void theFamilyHasMembers(int count) {
    List<Object> members = api.jsonObject("$.members");
    assertThat(members).hasSize(count);
  }

  @Then("{string} is a member of the family with role {string}")
  public void isAMemberOfTheFamily(String memberName, String familyRole) {
    users.getFamilyMembership(memberName, memberName);
    assertThat(api.jsonValue("$[0].familyId")).isEqualTo(String.valueOf(familyId));
    assertThat(api.jsonValue("$[0].userId")).isEqualTo(String.valueOf(users.idOf(memberName)));
    assertThat(api.jsonValue("$[0].familyRole")).isEqualTo(familyRole);
  }

  @Then("{string} does not belong to any family")
  public void doesNotBelongToAnyFamily(String memberName) {
    users.getFamilyMembership(memberName, memberName);
    List<Object> memberships = api.jsonObject("$");
    assertThat(memberships).isEmpty();
  }
}
