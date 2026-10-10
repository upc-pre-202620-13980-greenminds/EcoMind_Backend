package pe.greenminds.ecomind.gamification.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import pe.greenminds.ecomind.gamification.application.commandservices.RewardCommandService;
import pe.greenminds.ecomind.gamification.domain.model.commands.GrantQuestRewardCommand;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.gamification.domain.model.valueobjects.UserId;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AccountId;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.EmailAddress;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GamificationControllerTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private TokenService tokens;
    @Autowired private RewardCommandService rewards;

    @Test
    void progressRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/gamification/me/progress"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rewardsAndProgressAreScopedToTheJwtSubject() throws Exception {
        long rewardedUser = 901L;
        long otherUser = 902L;
        rewards.handle(
                new GrantQuestRewardCommand(
                        UUID.randomUUID(),
                        new UserId(rewardedUser),
                        Instant.now(),
                        LocalDate.of(2026, 10, 5),
                        true,
                        new Reward(20, 0)));

        mockMvc.perform(
                        get("/api/v1/gamification/me/progress")
                                .header(HttpHeaders.AUTHORIZATION, bearer(otherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(otherUser))
                .andExpect(jsonPath("$.totalEcopoints").value(0));
        mockMvc.perform(
                        get("/api/v1/gamification/me/rewards")
                                .header(HttpHeaders.AUTHORIZATION, bearer(otherUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(
                        get("/api/v1/gamification/me/progress")
                                .header(HttpHeaders.AUTHORIZATION, bearer(rewardedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEcopoints").value(20));
        mockMvc.perform(
                        get("/api/v1/gamification/me/rewards")
                                .header(HttpHeaders.AUTHORIZATION, bearer(rewardedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].beneficiaryId").value(rewardedUser));
    }

    private String bearer(long accountId) {
        var user =
                new AuthenticatedUser(
                        new AccountId(accountId), new EmailAddress(accountId + "@example.com"));
        return "Bearer " + tokens.issueAccessToken(user).value();
    }
}
