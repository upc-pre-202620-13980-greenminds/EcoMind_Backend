package pe.greenminds.ecomind.users.application.internal.queryservices;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.users.application.queryservices.FamilyQueryService;
import pe.greenminds.ecomind.users.domain.model.aggregates.Family;
import pe.greenminds.ecomind.users.domain.model.queries.GetAllFamiliesQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyMembersByUserQuery;
import pe.greenminds.ecomind.users.domain.model.queries.GetFamilyQuery;
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository;

@Service
@Transactional(readOnly = true)
public class FamilyQueryServiceImpl implements FamilyQueryService {

  private final FamilyRepository familyRepository;

  public FamilyQueryServiceImpl(FamilyRepository familyRepository) {
    this.familyRepository = familyRepository;
  }

  @Override
  public List<Family> handle(GetAllFamiliesQuery query) {
    return familyRepository.findAll();
  }

  @Override
  public Optional<Family> handle(GetFamilyQuery query) {
    return familyRepository.findById(query.familyId());
  }

  @Override
  public Optional<Family> handle(GetFamilyMembersByUserQuery query) {
    return familyRepository.findByMemberUserId(query.userId());
  }
}
