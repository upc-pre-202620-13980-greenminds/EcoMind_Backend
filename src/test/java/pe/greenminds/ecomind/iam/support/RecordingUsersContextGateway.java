package pe.greenminds.ecomind.iam.support;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.iam.application.outboundservices.UsersContextGateway;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.SocialRole;

/**
 * Users gateway for tests: records the profiles IAM asks to create.
 */
@Component
@Primary
@Profile("test")
public class RecordingUsersContextGateway implements UsersContextGateway {

  public record RequestedProfile(Long accountId, String name, SocialRole socialRole) {
  }

  private final List<RequestedProfile> requestedProfiles = new CopyOnWriteArrayList<>();

  @Override
  public void createProfile(AccountId accountId, String name, SocialRole socialRole) {
    requestedProfiles.add(new RequestedProfile(accountId.value(), name, socialRole));
  }

  public List<RequestedProfile> requestedProfiles() {
    return List.copyOf(requestedProfiles);
  }

  public void reset() {
    requestedProfiles.clear();
  }
}
