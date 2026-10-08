package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import java.time.*; import java.util.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.monetization.application.commandservices.GemWalletCommandService;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.*;
import pe.greenminds.ecomind.monetization.domain.repositories.*;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.entities.*;
import pe.greenminds.ecomind.monetization.infrastructure.persistence.jpa.repositories.*;

@Service
public class InventoryCommandService {
 private final CosmeticRepository cosmetics; private final MultiplierRepository multipliers; private final StreakProtectorRepository protectors;
 private final UserCosmeticPersistenceRepository owned; private final ProtectorInventoryPersistenceRepository protectorInventory;
 private final UserMultiplierPersistenceRepository activeMultipliers; private final GemWalletCommandService wallet;
 public InventoryCommandService(CosmeticRepository c,MultiplierRepository m,StreakProtectorRepository p,UserCosmeticPersistenceRepository o,ProtectorInventoryPersistenceRepository pi,UserMultiplierPersistenceRepository am,GemWalletCommandService w){cosmetics=c;multipliers=m;protectors=p;owned=o;protectorInventory=pi;activeMultipliers=am;wallet=w;}

 @Transactional public UserCosmeticPersistenceEntity buyCosmetic(Long user,UUID id,UUID request){
  if(owned.findByUserIdAndCosmeticId(user,id.toString()).isPresent()) throw new IllegalArgumentException("Cosmetic already owned");
  var item=cosmetics.findById(id).filter(x->x.active()).orElseThrow(()->new IllegalArgumentException("Cosmetic is unavailable"));
  wallet.debit(user,item.priceInGems(),GemMovementOrigin.COSMETIC,request);
  var e=new UserCosmeticPersistenceEntity(); e.setId(UUID.randomUUID().toString());e.setUserId(user);e.setCosmeticId(id.toString()); return owned.save(e);
 }
 @Transactional public void equipCosmetic(Long user,UUID id,boolean equip){
  var selected=owned.findByUserIdAndCosmeticId(user,id.toString()).orElseThrow(()->new IllegalArgumentException("Cosmetic is not owned"));
  if(equip) owned.findByUserId(user).forEach(x->{x.setEquipped(false);owned.save(x);}); selected.setEquipped(equip);owned.save(selected);
 }
 @Transactional public ProtectorInventoryPersistenceEntity buyProtector(Long user,UUID id,UUID request){
  var item=protectors.findById(id).filter(x->x.active()).orElseThrow(()->new IllegalArgumentException("Protector is unavailable"));
  wallet.debit(user,item.priceInGems(),GemMovementOrigin.STREAK_PROTECTOR,request);
  var e=protectorInventory.findByUserIdAndProtectorId(user,id.toString()).orElseGet(()->{var n=new ProtectorInventoryPersistenceEntity();n.setId(UUID.randomUUID().toString());n.setUserId(user);n.setProtectorId(id.toString());return n;}); e.setQuantity(e.getQuantity()+1);return protectorInventory.save(e);
 }
 @Transactional public boolean consumeProtector(Long user){var list=protectorInventory.lockAvailable(user);if(list.isEmpty())return false;var e=list.getFirst();e.setQuantity(e.getQuantity()-1);protectorInventory.save(e);return true;}
 @Transactional public UserMultiplierPersistenceEntity buyMultiplier(Long user,UUID id,UUID request){
  var item=multipliers.findById(id).filter(x->x.active()).orElseThrow(()->new IllegalArgumentException("Multiplier is unavailable"));
  wallet.debit(user,item.priceInGems(),GemMovementOrigin.MULTIPLIER,request);var now=Instant.now();var e=new UserMultiplierPersistenceEntity();e.setId(UUID.randomUUID().toString());e.setUserId(user);e.setMultiplierId(id.toString());e.setFactor(item.factor());e.setStartsAt(now);e.setExpiresAt(now.plus(Duration.ofMinutes(item.durationMinutes())));return activeMultipliers.save(e);
 }
 @Transactional public UserCosmeticPersistenceEntity grantCosmetic(Long user,UUID id){return owned.findByUserIdAndCosmeticId(user,id.toString()).orElseGet(()->{cosmetics.findById(id).orElseThrow(()->new IllegalArgumentException("Cosmetic not found"));var e=new UserCosmeticPersistenceEntity();e.setId(UUID.randomUUID().toString());e.setUserId(user);e.setCosmeticId(id.toString());return owned.save(e);});}
}
