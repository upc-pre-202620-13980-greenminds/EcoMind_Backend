package pe.greenminds.ecomind.monetization.interfaces.rest.resources;
import jakarta.validation.constraints.NotNull; import java.util.UUID;
public record BuyItemResource(@NotNull UUID itemId,@NotNull UUID requestId){}
