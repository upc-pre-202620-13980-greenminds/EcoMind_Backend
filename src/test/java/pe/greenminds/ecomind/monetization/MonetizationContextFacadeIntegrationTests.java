package pe.greenminds.ecomind.monetization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import pe.greenminds.ecomind.monetization.application.internal.commandservices.InventoryCommandService;
import pe.greenminds.ecomind.monetization.application.internal.eventhandlers.MonetizationCatalogSeed;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.UserCosmeticPersistenceRepository;
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;
import pe.greenminds.ecomind.users.interfaces.rest.UserProfileController;
import pe.greenminds.ecomind.users.interfaces.rest.resources.UserProfileResource;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@ActiveProfiles("test")
class MonetizationContextFacadeIntegrationTests {
  @Autowired private MonetizationContextFacade facade;
  @Autowired private UserProfileRepository profiles;
  @Autowired private UserCosmeticPersistenceRepository cosmetics;
  @Autowired private InventoryCommandService inventory;
  @Autowired private UserProfileController profileController;

  @Test
  void individualProfileOnlyShowsAnEquippedAvatar() {
    long userId = 880011L;
    profiles.save(UserProfile.create(new UserId(userId), "Avatar Test", SocialRole.STUDENT));
    UUID avatarId = MonetizationCatalogSeed.LEAF_AVATAR;
    facade.grantCosmeticReward(
        new MonetizationContextFacade.GrantCosmeticReward(UUID.randomUUID(), userId, avatarId));

    var profile = (UserProfileResource) profileController.getUserProfile(userId, null).getBody();
    assertNull(profile.avatarImageReference());

    inventory.equipCosmetic(userId, avatarId, true);
    profile = (UserProfileResource) profileController.getUserProfile(userId, null).getBody();
    assertEquals("avatar_leaf", profile.avatarImageReference());
  }

  @Test
  void gamificationRewardsAreIdempotent() {
    long userId = 880010L;
    profiles.save(UserProfile.create(new UserId(userId), "Facade Test", SocialRole.STUDENT));
    UUID rewardId = UUID.randomUUID();
    var credit = new MonetizationContextFacade.CreditRewardGems(rewardId, userId, 25);
    facade.creditRewardGems(credit);
    facade.creditRewardGems(credit);
    assertEquals(25, profiles.findById(new UserId(userId)).orElseThrow().getGemBalance());

    UUID awardId = UUID.randomUUID();
    var grant = new MonetizationContextFacade.GrantCosmeticReward(
        awardId, userId, MonetizationCatalogSeed.LEAF_AVATAR);
    facade.grantCosmeticReward(grant);
    facade.grantCosmeticReward(grant);
    assertEquals(1, cosmetics.findByUserId(userId).size());
  }
}
