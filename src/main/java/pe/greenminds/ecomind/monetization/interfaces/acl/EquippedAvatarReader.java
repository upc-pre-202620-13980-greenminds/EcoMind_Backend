package pe.greenminds.ecomind.monetization.interfaces.acl;

import java.util.Optional;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.UserCosmeticPersistenceEntity;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.CosmeticPersistenceRepository;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.UserCosmeticPersistenceRepository;

@Component
public class EquippedAvatarReader {

    private final UserCosmeticPersistenceRepository inventory;
    private final CosmeticPersistenceRepository catalog;

    public EquippedAvatarReader(
            UserCosmeticPersistenceRepository inventory, CosmeticPersistenceRepository catalog) {
        this.inventory = inventory;
        this.catalog = catalog;
    }

    public Optional<String> findImageReference(Long userId) {
        return inventory.findByUserId(userId).stream()
                .filter(UserCosmeticPersistenceEntity::isEquipped)
                .flatMap(item -> catalog.findById(item.getCosmeticId()).stream())
                .filter(cosmetic -> CosmeticType.AVATAR.name().equals(cosmetic.getType()))
                .map(cosmetic -> cosmetic.getImageUrl())
                .findFirst();
    }
}
