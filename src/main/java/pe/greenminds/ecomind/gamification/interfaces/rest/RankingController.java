package pe.greenminds.ecomind.gamification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pe.greenminds.ecomind.gamification.application.queryservices.RankingQueryService;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingEntry;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPage;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingTransaction;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.shared.infrastructure.documentation.openapi.configuration.OpenApiConfiguration;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(
        value = "/api/v1/gamification/rankings",
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification")
@SecurityRequirement(name = OpenApiConfiguration.BEARER_AUTH_SCHEME)
public class RankingController {
    private final RankingQueryService queries;

    public RankingController(RankingQueryService queries) {
        this.queries = queries;
    }

    @GetMapping("/types")
    @Operation(summary = "Get available ranking types")
    public List<RankingType> types() {
        return queries.types();
    }

    @GetMapping("/{type}/participants")
    @Operation(
            summary = "Get ranking participants and accumulated ecopoints",
            description =
                    "Ordered by identity, not position. FRIENDS includes the JWT holder and"
                            + " accepted friends. Pages start at zero; maximum size is 100.")
    public RankingPage<RankingEntry> participants(
            @Parameter(
                            schema =
                                    @Schema(
                                            implementation = String.class,
                                            allowableValues = {
                                                "LOCAL",
                                                "GLOBAL",
                                                "FRIENDS",
                                                "FAMILIES"
                                            }))
                    @PathVariable
                    RankingType type,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queries.participants(type, new UserId(principal.accountId()), page, size);
    }

    @GetMapping("/{type}/transactions")
    @Operation(
            summary = "Get ecopoint transactions for client-side period ranking",
            description =
                    "UTC from is inclusive and to is exclusive. Follow hasNext to aggregate all"
                        + " pages. Pages start at zero; maximum size is 100. Queries never grant"
                        + " rewards.")
    public RankingPage<RankingTransaction> transactions(
            @Parameter(
                            schema =
                                    @Schema(
                                            implementation = String.class,
                                            allowableValues = {
                                                "LOCAL",
                                                "GLOBAL",
                                                "FRIENDS",
                                                "FAMILIES"
                                            }))
                    @PathVariable
                    RankingType type,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return queries.transactions(
                type, new UserId(principal.accountId()), new RankingPeriod(from, to), page, size);
    }
}
