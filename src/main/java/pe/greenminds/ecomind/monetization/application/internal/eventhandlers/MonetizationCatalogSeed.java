package pe.greenminds.ecomind.monetization.application.internal.eventhandlers;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.domain.model.entities.*;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.CosmeticType;
import pe.greenminds.ecomind.monetization.domain.repositories.*;

@Component
public class MonetizationCatalogSeed implements ApplicationRunner {
  public static final UUID LEAF_AVATAR = UUID.fromString("10000000-0000-0000-0000-000000000001");
  public static final UUID XP_BOOST = UUID.fromString("20000000-0000-0000-0000-000000000001");
  public static final UUID STREAK_SHIELD = UUID.fromString("30000000-0000-0000-0000-000000000001");
  public static final UUID STARTER_GEMS = UUID.fromString("40000000-0000-0000-0000-000000000001");
  private final CosmeticRepository cosmetics; private final MultiplierRepository multipliers;
  private final StreakProtectorRepository protectors; private final GemPackageRepository packages;
  public MonetizationCatalogSeed(CosmeticRepository c,MultiplierRepository m,StreakProtectorRepository p,GemPackageRepository g){cosmetics=c;multipliers=m;protectors=p;packages=g;}
  @Override @Transactional public void run(ApplicationArguments args){
    if(cosmetics.findById(LEAF_AVATAR).isEmpty()) cosmetics.save(new Cosmetic(LEAF_AVATAR,"Leaf Guardian","Eco-friendly avatar",80,CosmeticType.AVATAR,"avatar_leaf",true));
    if(multipliers.findById(XP_BOOST).isEmpty()) multipliers.save(new Multiplier(XP_BOOST,"Double XP","Doubles experience for one hour",new BigDecimal("2.00"),60,150,true));
    if(protectors.findById(STREAK_SHIELD).isEmpty()) protectors.save(new StreakProtector(STREAK_SHIELD,"Streak Shield","Protects one missed day",100,true));
    if(packages.findById(STARTER_GEMS).isEmpty()) packages.save(new GemPackage(STARTER_GEMS,"Starter Gems",100,new BigDecimal("3.99"),"PEN",true));
  }
}
