package pe.greenminds.ecomind.gamification.domain.model.valueobjects;

import java.util.List;

public record RankingPage<T>(List<T> items, int page, int size, boolean hasNext) {
  public RankingPage { items = List.copyOf(items); }
}
