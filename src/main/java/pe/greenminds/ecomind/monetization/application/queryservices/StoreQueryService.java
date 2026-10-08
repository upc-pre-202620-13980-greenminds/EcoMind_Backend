package pe.greenminds.ecomind.monetization.application.queryservices;

import pe.greenminds.ecomind.monetization.domain.model.aggregates.Store;

public interface StoreQueryService {
  Store getCatalog();
}
