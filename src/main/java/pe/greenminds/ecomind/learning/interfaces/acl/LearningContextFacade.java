package pe.greenminds.ecomind.learning.interfaces.acl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.EducationalMaterialEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.FavoriteMaterialEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.EducationalMaterialRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.FavoriteMaterialRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.RecentlyViewedMaterialRepository;

/**
 * Read-only contract of Learning for the other bounded contexts. A pending material is a favorite
 * that the user has not opened since saving it (HU-025).
 */
@Service
@Transactional(readOnly = true)
public class LearningContextFacade {

  /** A favorite that was not opened yet, with what a reminder needs to show. */
  public record PendingMaterial(
      Long favoriteId, Long userId, Long materialId, String title, int durationMinutes) {}

  private final FavoriteMaterialRepository favorites;
  private final RecentlyViewedMaterialRepository recentlyViewed;
  private final EducationalMaterialRepository materials;

  public LearningContextFacade(
      FavoriteMaterialRepository favorites,
      RecentlyViewedMaterialRepository recentlyViewed,
      EducationalMaterialRepository materials) {
    this.favorites = favorites;
    this.recentlyViewed = recentlyViewed;
    this.materials = materials;
  }

  /** Favorites saved between the two instants that were not opened after being saved. */
  public List<PendingMaterial> findPendingMaterials(Instant savedAfter, Instant savedBefore) {
    return favorites.findAll().stream()
        .filter(favorite -> savedBetween(favorite, savedAfter, savedBefore))
        .filter(this::notOpenedSinceSaved)
        .flatMap(favorite -> toPending(favorite).stream())
        .toList();
  }

  private static boolean savedBetween(
      FavoriteMaterialEntity favorite, Instant savedAfter, Instant savedBefore) {
    if (favorite.getCreatedAt() == null) {
      return false;
    }
    Instant savedAt = favorite.getCreatedAt().toInstant();
    return !savedAt.isBefore(savedAfter) && !savedAt.isAfter(savedBefore);
  }

  private boolean notOpenedSinceSaved(FavoriteMaterialEntity favorite) {
    Instant savedAt = favorite.getCreatedAt().toInstant();
    return recentlyViewed
        .findByUserIdAndMaterialId(favorite.getUserId(), favorite.getMaterialId())
        .map(view -> view.getViewedAt().isBefore(savedAt))
        .orElse(true);
  }

  private Optional<PendingMaterial> toPending(FavoriteMaterialEntity favorite) {
    return materials
        .findById(favorite.getMaterialId())
        .filter(EducationalMaterialEntity::isActive)
        .map(
            material ->
                new PendingMaterial(
                    favorite.getId(),
                    favorite.getUserId(),
                    material.getId(),
                    material.getTitle(),
                    material.getDurationMinutes()));
  }
}
