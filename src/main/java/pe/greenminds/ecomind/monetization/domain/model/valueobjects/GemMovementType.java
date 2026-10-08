package pe.greenminds.ecomind.monetization.domain.model.valueobjects;

public enum GemMovementType {
  PURCHASE_DEBIT,
  GEM_PURCHASE_CREDIT,
  REWARD_CREDIT,
  REFUND_CREDIT;

  public boolean isCredit() {
    return this != PURCHASE_DEBIT;
  }
}
