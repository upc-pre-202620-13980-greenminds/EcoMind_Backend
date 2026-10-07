package pe.greenminds.ecomind.monetization.domain.model.aggregates;

public class InsufficientGemBalanceException extends RuntimeException {
  public InsufficientGemBalanceException(int balance, int requested) {
    super("Insufficient gem balance: available " + balance + ", requested " + requested);
  }
}
