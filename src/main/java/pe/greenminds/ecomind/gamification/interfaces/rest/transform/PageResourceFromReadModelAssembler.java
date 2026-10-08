package pe.greenminds.ecomind.gamification.interfaces.rest.transform;

import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.PageResource;

import java.util.function.Function;

public final class PageResourceFromReadModelAssembler {
    private PageResourceFromReadModelAssembler() {}

    public static <T, R> PageResource<R> toResourceFromReadModel(
            RankingPage<T> page, Function<T, R> assembler) {
        return new PageResource<>(
                page.items().stream().map(assembler).toList(),
                page.page(),
                page.size(),
                page.hasNext());
    }
}
