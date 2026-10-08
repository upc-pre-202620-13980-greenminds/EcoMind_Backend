package pe.greenminds.ecomind.gamification.interfaces.rest.resources;

import java.util.List;

/** REST pagination contract, independent of the application read model. */
public record PageResource<T>(List<T> items, int page, int size, boolean hasNext) {
    public PageResource {
        items = List.copyOf(items);
    }
}
