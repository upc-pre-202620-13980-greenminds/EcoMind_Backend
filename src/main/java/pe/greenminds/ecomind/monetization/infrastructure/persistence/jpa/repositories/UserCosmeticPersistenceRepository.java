package pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories;
import java.util.*; import org.springframework.data.jpa.repository.JpaRepository; import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.UserCosmeticPersistenceEntity;
public interface UserCosmeticPersistenceRepository extends JpaRepository<UserCosmeticPersistenceEntity,String>{ Optional<UserCosmeticPersistenceEntity> findByUserIdAndCosmeticId(Long u,String c); List<UserCosmeticPersistenceEntity> findByUserId(Long u); }
