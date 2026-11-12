package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;
import pe.greenminds.ecomind.community.infrastructure.persistence.jpa.entities.CommunityGoalPersistenceEntity;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityGoalPersistenceRepository extends JpaRepository<CommunityGoalPersistenceEntity,Long>{
    List<CommunityGoalPersistenceEntity> findByCommunityId(Long id);
    boolean existsByCommunityIdAndStatus(Long id, CommunityGoalStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select goal from CommunityGoalPersistenceEntity goal where goal.id = :id")
    Optional<CommunityGoalPersistenceEntity> findByIdForUpdate(@Param("id") Long id);
}
