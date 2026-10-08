package pe.greenminds.ecomind.monetization.interfaces.rest.resources;

import pe.greenminds.ecomind.monetization.domain.model.aggregates.GemWallet;

public record GemWalletResource(int balance) {
  public static GemWalletResource from(GemWallet wallet) {
    return new GemWalletResource(wallet.balance());
  }
}
