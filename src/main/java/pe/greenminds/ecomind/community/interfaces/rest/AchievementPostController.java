package pe.greenminds.ecomind.community.interfaces.rest;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.community.application.internal.commandservices.AchievementPublicationService;
import pe.greenminds.ecomind.community.interfaces.rest.resources.AchievementPostResource;
import pe.greenminds.ecomind.community.interfaces.rest.transform.AchievementPostResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/Community/Communities/{communityId}/AchievementPosts")
@Tag(name = "Community Achievement Posts")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class AchievementPostController {
    private final AchievementPublicationService publications;

    public AchievementPostController(AchievementPublicationService publications) {
        this.publications = publications;
    }

    @GetMapping
    public List<AchievementPostResource> find(
            @PathVariable Long communityId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) UUID awardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return publications
                .findPosts(communityId, principal.accountId(), authorId, awardId, page, size)
                .stream()
                .map(AchievementPostResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }
}
