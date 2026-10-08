package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.queryservices.CommunityAchievementQueryService;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityAchievementsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityAchievementResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CommunityAchievementResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;

@RestController
@Tag(name="Community Achievement",description="Achievements earned by communities")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
@RequestMapping(value="/api/v1/Community/Achievements",produces=MediaType.APPLICATION_JSON_VALUE)
public class CommunityAchievementController{
    private final CommunityAchievementQueryService communityAchievementQueryService;
    public CommunityAchievementController(CommunityAchievementQueryService communityAchievementQueryService){
        this.communityAchievementQueryService = communityAchievementQueryService;
    }

    @GetMapping
    public List<CommunityAchievementResource> list(@RequestParam(required=false)Long community_id){
        return communityAchievementQueryService.handle(new SearchCommunityAchievementsQuery(community_id)).stream()
                .map(CommunityAchievementResourceFromEntityAssembler::toResourceFromEntity).toList();
    }
}
