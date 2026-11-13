package pe.greenminds.ecomind.learning.application;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.learning.domain.model.MaterialCategory;
import pe.greenminds.ecomind.learning.domain.model.MaterialType;
import pe.greenminds.ecomind.learning.domain.model.RecommendedAge;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.EducationalMaterialEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.FavoriteMaterialEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.entities.RecentlyViewedMaterialEntity;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.EducationalMaterialRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.FavoriteMaterialRepository;
import pe.greenminds.ecomind.learning.infrastructure.persistence.jpa.repositories.RecentlyViewedMaterialRepository;
import java.time.Instant;
import pe.greenminds.ecomind.learning.interfaces.rest.resources.EducationalMaterialResource;
import pe.greenminds.ecomind.learning.interfaces.rest.resources.SaveEducationalMaterialResource;

@Service
public class LearningService {
  private final EducationalMaterialRepository materials;
  private final FavoriteMaterialRepository favorites;
  private final RecentlyViewedMaterialRepository recentlyViewed;

  public LearningService(EducationalMaterialRepository materials, FavoriteMaterialRepository favorites,
      RecentlyViewedMaterialRepository recentlyViewed) {
    this.materials = materials;
    this.favorites = favorites;
    this.recentlyViewed = recentlyViewed;
  }

  @Transactional(readOnly = true)
  public List<EducationalMaterialResource> search(Long userId, String query, MaterialCategory category,
      MaterialType type, String language, Boolean downloadable, RecommendedAge recommendedAge,
      Integer minDurationMinutes, Integer maxDurationMinutes) {
    var favoriteIds = favoriteIds(userId);
    var normalized = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
    return materials.findAllByActiveTrueOrderByTitleAsc().stream()
        .filter(m -> language == null || language.isBlank() || language.equalsIgnoreCase(m.getLanguage()))
        .filter(m -> category == null || category == m.getCategory())
        .filter(m -> type == null || type == m.getMaterialType())
        .filter(m -> downloadable == null || downloadable == m.isDownloadable())
        .filter(m -> recommendedAge == null || recommendedAge == m.getRecommendedAge())
        .filter(m -> minDurationMinutes == null || m.getDurationMinutes() >= minDurationMinutes)
        .filter(m -> maxDurationMinutes == null || m.getDurationMinutes() <= maxDurationMinutes)
        .filter(m -> normalized.isBlank() || m.getTitle().toLowerCase(Locale.ROOT).contains(normalized)
            || m.getDescription().toLowerCase(Locale.ROOT).contains(normalized))
        .map(m -> EducationalMaterialResource.from(m, favoriteIds.contains(m.getId())))
        .toList();
  }

  @Transactional
  public EducationalMaterialResource get(Long id, Long userId) {
    var material = materials.findById(id).filter(EducationalMaterialEntity::isActive)
        .orElseThrow(() -> new NoSuchElementException("Educational material was not found"));
    var visit = recentlyViewed.findByUserIdAndMaterialId(userId, id).orElseGet(() -> {
      var entity = new RecentlyViewedMaterialEntity(); entity.setUserId(userId); entity.setMaterialId(id); return entity;
    });
    visit.setViewedAt(Instant.now()); recentlyViewed.save(visit);
    return EducationalMaterialResource.from(material,
        favorites.findByUserIdAndMaterialId(userId, id).isPresent());
  }

  @Transactional(readOnly = true)
  public List<EducationalMaterialResource> recentlyViewed(Long userId) {
    var favoriteIds = favoriteIds(userId);
    return recentlyViewed.findAllByUserIdOrderByViewedAtDesc(userId).stream()
        .map(v -> materials.findById(v.getMaterialId()).orElse(null))
        .filter(Objects::nonNull).filter(EducationalMaterialEntity::isActive)
        .map(m -> EducationalMaterialResource.from(m, favoriteIds.contains(m.getId()))).toList();
  }

  @Transactional(readOnly = true)
  public List<EducationalMaterialResource> favorites(Long userId) {
    var ids = favoriteIds(userId);
    return materials.findAllById(ids).stream().filter(EducationalMaterialEntity::isActive)
        .sorted(Comparator.comparing(EducationalMaterialEntity::getTitle))
        .map(m -> EducationalMaterialResource.from(m, true)).toList();
  }

  @Transactional
  public boolean toggleFavorite(Long materialId, Long userId) {
    if (!materials.existsById(materialId)) throw new NoSuchElementException("Educational material was not found");
    var current = favorites.findByUserIdAndMaterialId(userId, materialId);
    if (current.isPresent()) {
      favorites.delete(current.get());
      return false;
    }
    var favorite = new FavoriteMaterialEntity();
    favorite.setUserId(userId);
    favorite.setMaterialId(materialId);
    favorites.save(favorite);
    return true;
  }

  @Transactional
  public EducationalMaterialResource create(SaveEducationalMaterialResource request) {
    if (materials.existsByTitleIgnoreCaseAndLanguage(request.title(), request.language())) {
      throw new IllegalArgumentException("An educational material with that title and language already exists");
    }
    return EducationalMaterialResource.from(materials.save(apply(new EducationalMaterialEntity(), request)), false);
  }

  @Transactional
  public EducationalMaterialResource update(Long id, SaveEducationalMaterialResource request, Long userId) {
    var entity = materials.findById(id).orElseThrow(() -> new NoSuchElementException("Educational material was not found"));
    return EducationalMaterialResource.from(materials.save(apply(entity, request)),
        favorites.findByUserIdAndMaterialId(userId, id).isPresent());
  }

  @Transactional
  public void delete(Long id) {
    var entity = materials.findById(id).orElseThrow(() -> new NoSuchElementException("Educational material was not found"));
    favorites.deleteAllByMaterialId(id);
    recentlyViewed.deleteAllByMaterialId(id);
    entity.setActive(false);
    materials.save(entity);
  }

  private Set<Long> favoriteIds(Long userId) {
    var ids = new HashSet<Long>();
    favorites.findAllByUserId(userId).forEach(f -> ids.add(f.getMaterialId()));
    return ids;
  }

  private EducationalMaterialEntity apply(EducationalMaterialEntity entity, SaveEducationalMaterialResource request) {
    entity.setTitle(request.title().trim()); entity.setDescription(request.description().trim());
    entity.setContent(request.content()); entity.setMaterialType(request.materialType());
    entity.setCategory(request.category()); entity.setImageUrl(request.imageUrl());
    entity.setVideoUrl(request.videoUrl()); entity.setDurationMinutes(request.durationMinutes());
    entity.setLanguage(request.language()); entity.setDownloadable(request.downloadable()); entity.setActive(true);
    entity.setSourceName(request.sourceName()); entity.setSourceUrl(request.sourceUrl());
    entity.setAuthor(request.author()); entity.setLicense(request.license());
    entity.setRecommendedAge(request.recommendedAge());
    return entity;
  }
}
