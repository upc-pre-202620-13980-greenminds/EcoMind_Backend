package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import pe.greenminds.ecomind.community.application.queryservices.CommunityAchievementQueryService;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunityAchievementsQuery;
import pe.greenminds.ecomind.community.interfaces.rest.resources.CommunityAchievementResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.CommunityAchievementResourceFromEntityAssembler;

@RestController
@Tag(name="Community Achievement",description="Achievements earned by communities")
@RequestMapping(value="/api/v1/Community/Achievements",produces=MediaType.APPLICATION_JSON_VALUE)
public class CommunityAchievementController{
    private final CommunityAchievementQueryService service;
    public CommunityAchievementController(CommunityAchievementQueryService s){
        service=s;
    }

    @GetMapping
    public List<CommunityAchievementResource> list(@RequestParam(required=false)Long community_id){
        return service.handle(new SearchCommunityAchievementsQuery(community_id)).stream().map(CommunityAchievementResourceFromEntityAssembler::toResourceFromEntity).toList();
    }
}
