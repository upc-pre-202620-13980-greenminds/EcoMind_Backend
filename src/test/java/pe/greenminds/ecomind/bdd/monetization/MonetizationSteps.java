package pe.greenminds.ecomind.bdd.monetization;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.*;
import java.util.Map;
import java.util.UUID;
import pe.greenminds.ecomind.bdd.ApiClient;
import pe.greenminds.ecomind.bdd.users.UsersApiDriver;
import pe.greenminds.ecomind.monetization.application.internal.eventhandlers.MonetizationCatalogSeed;

public class MonetizationSteps {
  private final ApiClient api;
  private final UsersApiDriver users;

  public MonetizationSteps(ApiClient api, UsersApiDriver users) {
    this.api = api;
    this.users = users;
  }

  @When("{string} opens the store catalog")
  public void catalog(String actor) {
    api.get("/api/v1/monetization/store", users.tokenOf(actor));
  }

  @Then("the store lists gem packages and personalization products")
  public void products() {
    for (String field :
        new String[] {"cosmetics", "multipliers", "streakProtectors", "gemPackages"}) {
      java.util.List<?> products = api.jsonObject("$." + field);
      assertThat(products).isNotEmpty();
    }
  }

  @When("{string} checks the gem wallet")
  public void wallet(String actor) {
    api.get("/api/v1/monetization/me/wallet", users.tokenOf(actor));
  }

  @Then("the gem wallet balance is {int}")
  public void balance(int value) {
    assertThat(api.jsonValue("$.balance")).isEqualTo(Integer.toString(value));
  }

  @When("{string} buys a streak protector without enough gems")
  public void purchase(String actor) {
    api.post(
        "/api/v1/monetization/me/protectors/purchases",
        Map.of(
            "itemId",
            MonetizationCatalogSeed.STREAK_SHIELD.toString(),
            "requestId",
            UUID.randomUUID().toString()),
        users.tokenOf(actor));
  }

  @When("{string} submits a purchase without an idempotency key")
  public void noKey(String actor) {
    api.post(
        "/api/v1/monetization/me/protectors/purchases",
        Map.of("itemId", MonetizationCatalogSeed.STREAK_SHIELD.toString()),
        users.tokenOf(actor));
  }

  @When("{string} checks the inventory")
  public void inventory(String actor) {
    api.get("/api/v1/monetization/me/inventory", users.tokenOf(actor));
  }

  @Then("the inventory has no purchased items")
  public void empty() {
    for (String field : new String[] {"cosmetics", "multipliers", "protectors"}) {
      java.util.List<?> items = api.jsonObject("$." + field);
      assertThat(items).isEmpty();
    }
  }
}
