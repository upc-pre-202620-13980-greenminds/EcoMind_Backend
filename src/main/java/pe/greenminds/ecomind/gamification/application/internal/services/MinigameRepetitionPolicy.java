package pe.greenminds.ecomind.gamification.application.internal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Component
public class MinigameRepetitionPolicy {
    private final List<BigDecimal> factors;

    public MinigameRepetitionPolicy(
            @Value("${gamification.minigame.repetition-factors:}") String configured) {
        factors =
                configured.isBlank()
                        ? List.of()
                        : Arrays.stream(configured.split(","))
                                .map(String::trim)
                                .map(BigDecimal::new)
                                .toList();
        if (!factors.isEmpty()) {
            if (factors.getFirst().compareTo(BigDecimal.ONE) != 0)
                throw new IllegalArgumentException("First attempt must receive its full reward");
            for (int i = 1; i < factors.size(); i++)
                if (factors.get(i).signum() < 0
                        || factors.get(i).compareTo(factors.get(i - 1)) >= 0)
                    throw new IllegalArgumentException(
                            "Repetition factors must decrease towards zero");
        }
    }

    public BigDecimal factor(long priorAttempts) {
        if (factors.isEmpty())
            throw new IllegalStateException(
                    "Configure gamification.minigame.repetition-factors before granting minigame"
                            + " rewards");
        return priorAttempts < factors.size()
                ? factors.get(Math.toIntExact(priorAttempts))
                : BigDecimal.ZERO;
    }
}
