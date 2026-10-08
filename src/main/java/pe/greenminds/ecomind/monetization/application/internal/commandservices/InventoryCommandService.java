package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
 private final ProcessedMonetizationRequestPersistenceRepository processed;
 public InventoryCommandService(CosmeticRepository c,MultiplierRepository m,StreakProtectorRepository p,UserCosmeticPersistenceRepository o,ProtectorInventoryPersistenceRepository pi,UserMultiplierPersistenceRepository am,GemWalletCommandService w,ProcessedMonetizationRequestPersistenceRepository processed){cosmetics=c;multipliers=m;protectors=p;owned=o;protectorInventory=pi;activeMultipliers=am;wallet=w;this.processed=processed;}

 @Transactional public UserCosmeticPersistenceEntity buyCosmetic(Long user,UUID id,UUID request){
  var replay=replay(request,"COSMETIC_PURCHASE",user,id,owned::findById);if(replay!=null)return replay;
  if(owned.findByUserIdAndCosmeticId(user,id.toString()).isPresent()) throw new IllegalArgumentException("Cosmetic already owned");
  var item=cosmetics.findById(id).filter(x->x.active()).orElseThrow(()->new IllegalArgumentException("Cosmetic is unavailable"));
  wallet.debit(user,item.priceInGems(),GemMovementOrigin.COSMETIC,request);
  var e=new UserCosmeticPersistenceEntity(); e.setId(UUID.randomUUID().toString());e.setUserId(user);e.setCosmeticId(id.toString());var saved=owned.save(e);mark(request,"COSMETIC_PURCHASE",user,id,saved.getId());return saved;
 }
 @Transactional public void equipCosmetic(Long user,UUID id,boolean equip){
  var selected=owned.findByUserIdAndCosmeticId(user,id.toString()).orElseThrow(()->new IllegalArgumentException("Cosmetic is not owned"));
  if(equip) owned.findByUserId(user).forEach(x->{x.setEquipped(false);owned.save(x);}); selected.setEquipped(equip);owned.save(selected);
 }
 @Transactional public ProtectorInventoryPersistenceEntity buyProtector(Long user,UUID id,UUID request){
  var replay=replay(request,"PROTECTOR_PURCHASE",user,id,protectorInventory::findById);if(replay!=null)return replay;
  var item=protectors.findById(id).filter(x->x.active()).orElseThrow(()->new IllegalArgumentException("Protector is unavailable"));
  wallet.debit(user,item.priceInGems(),GemMovementOrigin.STREAK_PROTECTOR,request);
  var e=protectorInventory.findByUserIdAndProtectorId(user,id.toString()).orElseGet(()->{var n=new ProtectorInventoryPersistenceEntity();n.setId(UUID.randomUUID().toString());n.setUserId(user);n.setProtectorId(id.toString());return n;}); e.setQuantity(e.getQuantity()+1);var saved=protectorInventory.save(e);mark(request,"PROTECTOR_PURCHASE",user,id,saved.getId());return saved;
 }
 @Transactional public boolean consumeProtector(Long user){var list=protectorInventory.lockAvailable(user);if(list.isEmpty())return false;var e=list.getFirst();e.setQuantity(e.getQuantity()-1);protectorInventory.save(e);return true;}
 @Transactional public UserMultiplierPersistenceEntity buyMultiplier(Long user,UUID id,UUID request){
  var replay=replay(request,"MULTIPLIER_PURCHASE",user,id,activeMultipliers::findById);if(replay!=null)return replay;
  var item=multipliers.findById(id).filter(x->x.active()).orElseThrow(()->new IllegalArgumentException("Multiplier is unavailable"));
  wallet.debit(user,item.priceInGems(),GemMovementOrigin.MULTIPLIER,request);var now=Instant.now();var e=new UserMultiplierPersistenceEntity();e.setId(UUID.randomUUID().toString());e.setUserId(user);e.setMultiplierId(id.toString());e.setFactor(item.factor());e.setStartsAt(now);e.setExpiresAt(now.plusSeconds(item.durationMinutes()*60L));var saved=activeMultipliers.save(e);mark(request,"MULTIPLIER_PURCHASE",user,id,saved.getId());return saved;
 }
 @Transactional public UserCosmeticPersistenceEntity grantCosmetic(Long user,UUID id){return owned.findByUserIdAndCosmeticId(user,id.toString()).orElseGet(()->{cosmetics.findById(id).orElseThrow(()->new IllegalArgumentException("Cosmetic not found"));var e=new UserCosmeticPersistenceEntity();e.setId(UUID.randomUUID().toString());e.setUserId(user);e.setCosmeticId(id.toString());return owned.save(e);});}

 private <T> T replay(UUID request,String operation,Long user,UUID target,Function<String,java.util.Optional<T>> finder){
  var prior=processed.findById(request.toString());if(prior.isEmpty())return null;var p=prior.get();
  if(!operation.equals(p.getOperation())||!user.equals(p.getUserId())||!target.toString().equals(p.getTargetId()))throw new IllegalArgumentException("Request id was already used for another operation");
  return finder.apply(p.getResultId()).orElseThrow(()->new IllegalStateException("Processed purchase result is missing"));
 }
 private void mark(UUID request,String operation,Long user,UUID target,String result){var p=new ProcessedMonetizationRequestPersistenceEntity();p.setRequestId(request.toString());p.setOperation(operation);p.setUserId(user);p.setTargetId(target.toString());p.setResultId(result);p.setProcessedAt(Instant.now());processed.save(p);}
}
