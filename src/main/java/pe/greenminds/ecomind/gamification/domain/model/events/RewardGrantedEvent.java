package pe.greenminds.ecomind.gamification.domain.model.events;

import pe.greenminds.ecomind.gamification.domain.model.aggregates.RewardTransaction;

public record RewardGrantedEvent(RewardTransaction transaction) {}
