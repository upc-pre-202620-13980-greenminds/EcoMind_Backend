package pe.greenminds.ecomind.monetization.application.outboundservices;

/** Anti-corruption port: Users remains the sole owner of the gem balance. */
public interface UserGemBalanceGateway {
  int getBalance(Long userId);
  int credit(Long userId, int amount);
  int debit(Long userId, int amount);
}
