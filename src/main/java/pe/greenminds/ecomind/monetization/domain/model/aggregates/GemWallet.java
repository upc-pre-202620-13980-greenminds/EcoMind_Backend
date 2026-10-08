package pe.greenminds.ecomind.monetization.domain.model.aggregates;

/** The gem balance owned by one EcoMind account. */
public record GemWallet(Long userId, int balance) {
  public GemWallet {
    if (userId == null || userId <= 0) throw new IllegalArgumentException("User id must be positive");
    if (balance < 0) throw new IllegalArgumentException("Gem balance cannot be negative");
  }

  public GemWallet credit(int amount) {
    requirePositive(amount);
    return new GemWallet(userId, Math.addExact(balance, amount));
  }

  public GemWallet debit(int amount) {
    requirePositive(amount);
    if (amount > balance) throw new InsufficientGemBalanceException(balance, amount);
    return new GemWallet(userId, balance - amount);
  }

  private static void requirePositive(int amount) {
    if (amount <= 0) throw new IllegalArgumentException("Gem amount must be positive");
  }
}
