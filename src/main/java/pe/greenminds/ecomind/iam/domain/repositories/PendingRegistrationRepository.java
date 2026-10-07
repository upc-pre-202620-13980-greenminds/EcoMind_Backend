package pe.greenminds.ecomind.iam.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PendingRegistration;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

public interface PendingRegistrationRepository {

  PendingRegistration save(PendingRegistration pendingRegistration);

  Optional<PendingRegistration> findByEmail(EmailAddress email);

  /** Removes the pending registration of an email so a new one can replace it. */
  void deleteByEmail(EmailAddress email);
}
