package pe.greenminds.ecomind.community.application.queryservices;

import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalPanel;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface CommunityGoalPanelQueryService {
    Result<CommunityGoalPanel, ApplicationError> get(Long communityId, Long userId);
}