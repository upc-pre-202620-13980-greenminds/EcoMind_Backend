package pe.greenminds.ecomind.gamification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.gamification.application.queryservices.AchievementQueryService;
import pe.greenminds.ecomind.gamification.application.queryservices.RankingQueryService;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.AchievementScope;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.gamification.interfaces.rest.resources.AchievementResource;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.AchievementAwardResourceFromEntityAssembler;
import pe.greenminds.ecomind.gamification.interfaces.rest.transform.AchievementResourceFromEntityAssembler;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification compatibility")
@SecurityRequirement(name = "bearerAuth")
public class LegacyGamificationController {
    private final AchievementQueryService achievements;
    private final RankingQueryService rankings;
    private final ResponseEntityAssembler responses;

    public LegacyGamificationController(
            AchievementQueryService achievements,
            RankingQueryService rankings,
            ResponseEntityAssembler responses) {
        this.achievements = achievements;
        this.rankings = rankings;
        this.responses = responses;
    }

    @GetMapping("/achievement")
    @Operation(summary = "TS-006: browse achievement definitions")
    public List<AchievementResource> catalog(
            @RequestParam(required = false) AchievementScope scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return achievements.search(scope, page, size).stream()
                .map(AchievementResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
    }

    @GetMapping("/user_achievement")
    @Operation(summary = "TS-006: get my individual achievement awards")
    public ResponseEntity<?> own(
            @RequestParam(name = "user_id", required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        if (userId != null && !userId.equals(principal.accountId()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(
                            ApplicationError.forbidden(
                                    "ACHIEVEMENT_OWNER_REQUIRED",
                                    "Only own individual awards are available"));
        return ResponseEntity.ok(
                achievements.forUser(new UserId(principal.accountId()), page, size).stream()
                        .map(AchievementAwardResourceFromEntityAssembler::toResourceFromEntity)
                        .toList());
    }

    @GetMapping("/community_achievement")
    @Operation(summary = "TS-006: get collective community awards")
    public ResponseEntity<?> collective(
            @RequestParam(name = "community_id") Long communityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return responses.toResponseEntityFromResult(
                achievements.forCommunity(
                        communityId, new UserId(principal.accountId()), page, size),
                awards ->
                        awards.stream()
                                .map(
                                        AchievementAwardResourceFromEntityAssembler
                                                ::toResourceFromEntity)
                                .toList(),
                HttpStatus.OK);
    }

    @GetMapping("/ranking")
    @Operation(summary = "TS-007: get available ranking types")
    public List<RankingType> types() {
        return rankings.types();
    }

    @GetMapping("/ecopoint_transaction")
    @Operation(
            summary = "TS-007: get authorized transactions for weekly ranking",
            description = "Android computes positions. Inclusive from, exclusive to.")
    public RankingPage<RankingTransaction> transactions(
            @RequestParam(defaultValue = "GLOBAL") RankingType type,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        return rankings.transactions(
                type, new UserId(principal.accountId()), new RankingPeriod(from, to), page, size);
    }
}
