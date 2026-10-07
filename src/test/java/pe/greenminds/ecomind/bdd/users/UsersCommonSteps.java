package pe.greenminds.ecomind.bdd.users;

import io.cucumber.java.en.Given;

/**
 * Steps shared by the Users features: the people who take part in a scenario.
 */
public class UsersCommonSteps {

  private final UsersApiDriver users;

  public UsersCommonSteps(UsersApiDriver users) {
    this.users = users;
  }

  @Given("the parent {string} is registered")
  public void theParentIsRegistered(String name) {
    users.register(name, "PARENT");
  }

  @Given("the student {string} is registered")
  public void theStudentIsRegistered(String name) {
    users.register(name, "STUDENT");
  }
}
