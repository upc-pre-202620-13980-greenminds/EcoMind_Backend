package pe.greenminds.ecomind.monetization.domain.model.aggregates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GemWalletTests {
  @Test
  void creditsAndDebitsGems() {
    var wallet = new GemWallet(10L, 20).credit(15).debit(5);
    assertEquals(30, wallet.balance());
  }

  @Test
  void rejectsDebitWhenBalanceIsInsufficient() {
    var wallet = new GemWallet(10L, 20);
    assertThrows(InsufficientGemBalanceException.class, () -> wallet.debit(21));
  }

  @Test
  void rejectsInvalidWalletAndAmounts() {
    assertThrows(IllegalArgumentException.class, () -> new GemWallet(10L, -1));
    assertThrows(IllegalArgumentException.class, () -> new GemWallet(10L, 0).credit(0));
    assertThrows(IllegalArgumentException.class, () -> new GemWallet(10L, 0).debit(0));
  }
}
