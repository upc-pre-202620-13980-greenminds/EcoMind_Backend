package pe.greenminds.ecomind.gamification.application.outboundservices;

/** Missing supplier or configuration is retryable, not a negative business result. */
public class GamificationDependencyUnavailableException extends IllegalStateException {
    public GamificationDependencyUnavailableException(String message) {
        super(message);
    }
}
