package pe.greenminds.ecomind.community.domain.repositories;

import java.util.List;
import java.util.Optional;
import pe.greenminds.ecomind.community.domain.model.aggregates.Community;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunitiesQuery;

public interface CommunityRepository {
    Community save(Community community);

    Optional<Community> findById(Long id);

    List<Community> search(SearchCommunitiesQuery query);

    boolean existsById(Long id);
}
