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

import pe.greenminds.ecomind.gamification.application.queryservices.RewardQueryService;
import pe.greenminds.ecomind.gamification.domain.model.queries.GetRewardTransactionsQuery;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.BeneficiaryType;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RankingPeriod;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.RewardBeneficiary;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.shared.infrastructure.security.AuthenticatedUserPrincipal;
import pe.greenminds.ecomind.shared.interfaces.rest.transform.ResponseEntityAssembler;

import java.time.Instant;

@RestController
@RequestMapping(value = "/api/v1/gamification/rewards", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification")
@SecurityRequirement(name = "bearerAuth")
public class RewardController {
    private final RewardQueryService queries;
    private final ResponseEntityAssembler responses;

    public RewardController(RewardQueryService queries, ResponseEntityAssembler responses) {
        this.queries = queries;
        this.responses = responses;
    }

    @GetMapping
    @Operation(
            summary = "Get authorized reward history for a beneficiary and period",
            description =
                    "Inclusive from, exclusive to. Pages start at zero; maximum size 100. USER"
                            + " defaults to the authenticated account.")
    public ResponseEntity<?> history(
            @RequestParam(defaultValue = "USER") BeneficiaryType beneficiaryType,
            @RequestParam(required = false) Long beneficiaryId,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal AuthenticatedUserPrincipal principal) {
        Long id =
                beneficiaryId == null && beneficiaryType == BeneficiaryType.USER
                        ? principal.accountId()
                        : beneficiaryId;
        var q =
                new GetRewardTransactionsQuery(
                        new RewardBeneficiary(beneficiaryType, id),
                        new UserId(principal.accountId()),
                        new RankingPeriod(from, to),
                        page,
                        size);
        return responses.toResponseEntityFromResult(
                queries.handle(q), result -> result, HttpStatus.OK);
    }
}
