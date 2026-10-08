package pe.greenminds.ecomind.monetization.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pe.greenminds.ecomind.monetization.application.outboundservices.UserGemBalanceGateway;
import pe.greenminds.ecomind.monetization.domain.model.aggregates.InsufficientGemBalanceException;
import pe.greenminds.ecomind.monetization.domain.model.entities.GemMovement;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementOrigin;
import pe.greenminds.ecomind.monetization.domain.model.valueobjects.GemMovementType;
import pe.greenminds.ecomind.monetization.domain.repositories.GemMovementRepository;

class GemWalletCommandServiceImplTests {
  @Test
  void creditsWalletAndRecordsMovement() {
    var wallets = Mockito.mock(UserGemBalanceGateway.class);
    var movements = Mockito.mock(GemMovementRepository.class);
    var reference = UUID.randomUUID();
    when(movements.findByReferenceId(reference)).thenReturn(Optional.empty());
    when(wallets.credit(7L, 25)).thenReturn(35);
    when(movements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var result = new GemWalletCommandServiceImpl(wallets, movements)
        .credit(7L, 25, GemMovementType.REWARD_CREDIT, GemMovementOrigin.QUEST, reference);

    assertEquals(35, result.balance());
    var captured = ArgumentCaptor.forClass(GemMovement.class);
    verify(movements).save(captured.capture());
    assertEquals(GemMovementType.REWARD_CREDIT, captured.getValue().type());
    assertEquals(35, captured.getValue().balanceAfter());
  }

  @Test
  void doesNotDebitWhenBalanceIsInsufficient() {
    var wallets = Mockito.mock(UserGemBalanceGateway.class);
    var movements = Mockito.mock(GemMovementRepository.class);
    var reference = UUID.randomUUID();
    when(movements.findByReferenceId(reference)).thenReturn(Optional.empty());
    when(wallets.debit(7L, 11)).thenThrow(new InsufficientGemBalanceException(10, 11));

    var service = new GemWalletCommandServiceImpl(wallets, movements);
    assertThrows(InsufficientGemBalanceException.class,
        () -> service.debit(7L, 11, GemMovementOrigin.COSMETIC, reference));
    verify(movements, never()).save(any());
  }

  @Test
  void repeatedReferenceDoesNotCreditTwice() {
    var wallets = Mockito.mock(UserGemBalanceGateway.class);
    var movements = Mockito.mock(GemMovementRepository.class);
    var reference = UUID.randomUUID();
    var prior = new GemMovement(UUID.randomUUID(), 7L, GemMovementType.REWARD_CREDIT,
        GemMovementOrigin.QUEST, 25, 35, reference, java.time.Instant.now());
    when(movements.findByReferenceId(reference)).thenReturn(Optional.of(prior));
    when(wallets.getBalance(7L)).thenReturn(35);

    var result = new GemWalletCommandServiceImpl(wallets, movements)
        .credit(7L, 25, GemMovementType.REWARD_CREDIT, GemMovementOrigin.QUEST, reference);

    assertEquals(35, result.balance());
    verify(wallets, never()).credit(any(), any(Integer.class));
    verify(movements, never()).save(any());
  }
}
