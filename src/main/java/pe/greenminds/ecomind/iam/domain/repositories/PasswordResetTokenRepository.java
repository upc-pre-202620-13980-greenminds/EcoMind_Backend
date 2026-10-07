package pe.greenminds.ecomind.iam.domain.repositories;

import java.util.Optional;
import pe.greenminds.ecomind.iam.domain.model.aggregates.PasswordResetToken;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.TokenHash;

public interface PasswordResetTokenRepository {

  PasswordResetToken save(PasswordResetToken passwordResetToken);

  Optional<PasswordResetToken> findByTokenHash(TokenHash tokenHash);
}
