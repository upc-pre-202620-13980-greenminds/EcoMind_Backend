package pe.greenminds.ecomind.quests.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanQueryService;
import pe.greenminds.ecomind.quests.application.queryservices.FamilyPlanState;
import pe.greenminds.ecomind.quests.domain.model.queries.GetActiveFamilyPlanByFamilyIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetFamilyPlanByIdQuery;
import pe.greenminds.ecomind.quests.domain.model.queries.GetFamilyPlansByFamilyIdQuery;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.FamilyPlanStatus;
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FamilyPlanQueryServiceImpl implements FamilyPlanQueryService {
    private final FamilyPlanRepository familyPlanRepository;
    private final FamilyPlanStateAssembler familyPlanStateAssembler;

    public FamilyPlanQueryServiceImpl(
            FamilyPlanRepository familyPlanRepository,
            FamilyPlanStateAssembler familyPlanStateAssembler
    ) {
        this.familyPlanRepository = familyPlanRepository;
        this.familyPlanStateAssembler = familyPlanStateAssembler;
    }

    @Override
    public Optional<FamilyPlanState> handle(GetFamilyPlanByIdQuery query) {
        return familyPlanRepository.findById(query.familyPlanId())
                .map(familyPlanStateAssembler::toState);
    }

    @Override
    public List<FamilyPlanState> handle(GetFamilyPlansByFamilyIdQuery query) {
        return familyPlanRepository.findByFamilyId(query.familyId())
                .stream()
                .map(familyPlanStateAssembler::toState)
                .toList();
    }

    @Override
    public Optional<FamilyPlanState> handle(GetActiveFamilyPlanByFamilyIdQuery query) {
        return familyPlanRepository.findByFamilyIdAndStatus(
                        query.familyId(),
                        FamilyPlanStatus.ACTIVE
                )
                .map(familyPlanStateAssembler::toState);
    }
}
