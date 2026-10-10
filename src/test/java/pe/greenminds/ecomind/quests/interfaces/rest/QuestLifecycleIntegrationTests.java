package pe.greenminds.ecomind.quests.interfaces.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.jayway.jsonpath.JsonPath;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import pe.greenminds.ecomind.iam.application.outboundservices.TokenService;
import pe.greenminds.ecomind.iam.domain.model.valueobjects.*;
import pe.greenminds.ecomind.users.domain.model.aggregates.UserProfile;
import pe.greenminds.ecomind.users.domain.model.valueobjects.*;
import pe.greenminds.ecomind.users.domain.model.valueobjects.SocialRole;
import pe.greenminds.ecomind.users.domain.repositories.UserProfileRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class QuestLifecycleIntegrationTests {
  static final AtomicLong sequence = new AtomicLong(910000);
  @Autowired MockMvc mvc;
  @Autowired TokenService tokens;
  @Autowired UserProfileRepository profiles;
  @Autowired pe.greenminds.ecomind.quests.domain.repositories.CollabQuestSessionRepository sessions;
  long owner, participant, outsider, family;

  @BeforeEach
  void users() throws Exception {
    owner = sequence.incrementAndGet();
    participant = sequence.incrementAndGet();
    outsider = sequence.incrementAndGet();
    for (long id : new long[] {owner, participant, outsider})
      profiles.save(
          UserProfile.create(
              new UserId(id), "Quest Test", id == owner ? SocialRole.PARENT : SocialRole.STUDENT));
    family =
        id(
            call(
                post("/api/v1/family"),
                owner,
                "{\"name\":\"Quest family\",\"commitment\":\"Save energy together\"}",
                201));
    call(
        post("/api/v1/family_user"),
        owner,
        "{\"familyId\":" + family + ",\"userId\":" + participant + ",\"familyRole\":\"CHILD\"}",
        201);
  }

  String call(MockHttpServletRequestBuilder request, long actor, String body, int status)
      throws Exception {
    var user =
        new AuthenticatedUser(new AccountId(actor), new EmailAddress(actor + "@example.com"));
    request.header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.issueAccessToken(user).value());
    if (body != null) request.contentType(MediaType.APPLICATION_JSON).content(body);
    var response = mvc.perform(request).andReturn().getResponse();
    assertEquals(status, response.getStatus(), response.getContentAsString());
    return response.getContentAsString();
  }

  long id(String json) {
    return ((Number) JsonPath.read(json, "$.id")).longValue();
  }

  String questBody(String type, String title) {
    return "{\"title\":\""
        + title
        + "\",\"description\":\"Check rooms\",\"category\":\"ENERGY\",\"type\":\""
        + type
        + "\",\"gemReward\":2,\"ecopoints\":10,\"age\":8,\"time\":2,\"theme\":\"CHECKBOX\"}";
  }

  long quest(String type) throws Exception {
    long q = id(call(post("/api/v1/quests"), owner, questBody(type, "Switch lights"), 201));
    call(
        post("/api/v1/activities"),
        owner,
        "{\"questId\":" + q + ",\"description\":\"Check room\",\"order\":1,\"type\":\"CHECKBOX\"}",
        201);
    call(patch("/api/v1/quests/" + q + "/publish"), owner, null, 200);
    return q;
  }

  long session(long quest) throws Exception {
    return id(
        call(post("/api/v1/collaborative-quests"), owner, "{\"questId\":" + quest + "}", 201));
  }

  long invite(long session) throws Exception {
    return id(
        call(
            post("/api/v1/collaborative-quest-members"),
            owner,
            "{\"sessionId\":" + session + ",\"invitedUserId\":" + participant + "}",
            201));
  }

  long plan(long quest) throws Exception {
    return id(
        call(
            post("/api/v1/family-plans"),
            owner,
            "{\"familyId\":" + family + ",\"items\":[{\"questId\":" + quest + "}]}",
            201));
  }

  String state(long quest, long actor) throws Exception {
    return call(get("/api/v1/collaborative-quests/state?questId=" + quest), actor, null, 200);
  }

  long assignment(long quest, long actor) throws Exception {
    return id(call(get("/api/v1/quest-users/me/quest/" + quest), actor, null, 200));
  }

  void submit(long assignment, long actor) throws Exception {
    String list = call(get("/api/v1/activity-users/quest-user/" + assignment), actor, null, 200);
    long activity = ((Number) JsonPath.read(list, "$[0].id")).longValue();
    call(
        post("/api/v1/activity-users/" + activity + "/submit"),
        actor,
        "{\"data\":{\"checked\":true}}",
        200);
  }

  long startCollaboration(long quest) throws Exception {
    long s = session(quest);
    long m = invite(s);
    call(patch("/api/v1/collaborative-quest-members/" + m + "/accept"), participant, null, 200);
    call(post("/api/v1/collaborative-quests/" + s + "/start"), owner, null, 200);
    return s;
  }

  @Test
  void collaborativeProgressSynchronizesAndCompletionRewardsEachMemberOnce() throws Exception {
    long q = quest("COLLABORATIVE"),
        s = startCollaboration(q),
        a = assignment(q, owner),
        b = assignment(q, participant);
    submit(a, owner);
    String peer = call(get("/api/v1/quest-users/" + b), participant, null, 200);
    assertEquals("READY_TO_COMPLETE", JsonPath.read(peer, "$.status"));
    call(post("/api/v1/quest-users/" + a + "/complete"), owner, null, 200);
    assertEquals(
        pe.greenminds.ecomind.quests.domain.model.valueobjects.CollabQuestStatus.COMPLETED,
        sessions.findById(s).orElseThrow().getStatus());
    for (long actor : new long[] {owner, participant})
      assertEquals(
          10,
          ((Number)
                  JsonPath.read(
                      call(get("/api/v1/gamification/me/progress"), actor, null, 200),
                      "$.totalEcopoints"))
              .intValue());
    call(post("/api/v1/quest-users/" + a + "/complete"), owner, null, 200);
    assertEquals(
        10,
        ((Number)
                JsonPath.read(
                    call(get("/api/v1/gamification/me/progress"), owner, null, 200),
                    "$.totalEcopoints"))
            .intValue());
  }

  @ParameterizedTest
  @ValueSource(strings = {"accept", "decline", "leave"})
  void unrelatedAccountCannotAnswerOrLeaveAnotherMembership(String action) throws Exception {
    long s = session(quest("COLLABORATIVE")), m = invite(s);
    call(patch("/api/v1/collaborative-quest-members/" + m + "/" + action), outsider, null, 403);
    assertEquals(
        "PENDING",
        JsonPath.read(
            call(get("/api/v1/collaborative-quest-members/" + m), participant, null, 200),
            "$.status"));
  }

  @Test
  void rejectedInvitationDoesNotStartSession() throws Exception {
    long q = quest("COLLABORATIVE"), s = session(q), m = invite(s);
    call(patch("/api/v1/collaborative-quest-members/" + m + "/decline"), participant, null, 200);
    call(post("/api/v1/collaborative-quests/" + s + "/start"), owner, null, 422);
    assertEquals("PENDING", JsonPath.read(state(q, owner), "$.session.status"));
  }

  @Test
  void ownerCanRevokeInvitationAndDeletePendingSession() throws Exception {
    long q = quest("COLLABORATIVE"), s = session(q), m = invite(s);
    call(patch("/api/v1/collaborative-quest-members/" + m + "/remove"), outsider, null, 422);
    call(patch("/api/v1/collaborative-quest-members/" + m + "/remove"), owner, null, 200);
    call(delete("/api/v1/collaborative-quests/" + s), owner, null, 204);
    assertNull(JsonPath.read(state(q, owner), "$.session"));
  }

  @Test
  void leavingStartedSessionRemovesOnlyDepartingMemberProgress() throws Exception {
    long q = quest("COLLABORATIVE"), s = startCollaboration(q), a = assignment(q, owner);
    String state = state(q, participant);
    long member = ((Number) JsonPath.read(state, "$.currentMember.id")).longValue();
    call(patch("/api/v1/collaborative-quest-members/" + member + "/leave"), participant, null, 200);
    call(get("/api/v1/quest-users/me/quest/" + q), participant, null, 404);
    call(get("/api/v1/quest-users/" + a), owner, null, 200);
    assertEquals("STARTED", JsonPath.read(state(q, owner), "$.session.status"));
  }

  @Test
  void startingTwiceDoesNotCreateDuplicateAssignments() throws Exception {
    long q = quest("COLLABORATIVE"), s = startCollaboration(q), a = assignment(q, owner);
    call(post("/api/v1/collaborative-quests/" + s + "/start"), owner, null, 422);
    assertEquals(a, assignment(q, owner));
  }

  @Test
  void familyPlanRunsFromDraftToSynchronizedCompletion() throws Exception {
    long q = quest("FAMILY"), p = plan(q);
    call(post("/api/v1/family-plans/" + p + "/activate"), owner, null, 200);
    call(post("/api/v1/family-plans/" + p + "/complete"), owner, null, 422);
    submit(assignment(q, participant), participant);
    call(post("/api/v1/family-plans/" + p + "/complete"), owner, null, 200);
    assertEquals(
        "COMPLETED",
        JsonPath.read(call(get("/api/v1/family-plans/" + p), owner, null, 200), "$.status"));
    call(post("/api/v1/family-plans/" + p + "/complete"), owner, null, 422);
  }

  @ParameterizedTest
  @ValueSource(strings = {"update", "activate", "delete"})
  void onlyOwnerCanChangeFamilyPlan(String action) throws Exception {
    long p = plan(quest("FAMILY"));
    switch (action) {
      case "update" -> call(put("/api/v1/family-plans/" + p), outsider, "{\"items\":[]}", 403);
      case "activate" -> call(post("/api/v1/family-plans/" + p + "/activate"), outsider, null, 403);
      default -> call(delete("/api/v1/family-plans/" + p), outsider, null, 403);
    }
    assertEquals(
        "DRAFT",
        JsonPath.read(call(get("/api/v1/family-plans/" + p), owner, null, 200), "$.status"));
  }

  @Test
  void activeFamilyPlanCancellationRemovesUnfinishedAssignments() throws Exception {
    long q = quest("FAMILY"), p = plan(q);
    call(post("/api/v1/family-plans/" + p + "/activate"), owner, null, 200);
    call(delete("/api/v1/family-plans/" + p), owner, null, 200);
    assertEquals(
        "CANCELLED",
        JsonPath.read(call(get("/api/v1/family-plans/" + p), owner, null, 200), "$.status"));
    call(get("/api/v1/quest-users/me/quest/" + q), participant, null, 404);
  }

  @Test
  void editingPublishedQuestKeepsExistingAssignmentOnOriginalVersion() throws Exception {
    long q = quest("ACTIVITIES");
    long a = id(call(post("/api/v1/quest-users"), participant, "{\"questId\":" + q + "}", 201));
    long next =
        id(call(put("/api/v1/quests/" + q), owner, questBody("ACTIVITIES", "New version"), 200));
    assertNotEquals(q, next);
    assertEquals(
        q,
        ((Number)
                JsonPath.read(
                    call(get("/api/v1/quest-users/" + a), participant, null, 200), "$.questId"))
            .longValue());
    call(patch("/api/v1/quests/" + next + "/publish"), owner, null, 200);
    submit(a, participant);
    call(post("/api/v1/quest-users/" + a + "/complete"), participant, null, 200);
  }

  long minigameQuest() throws Exception {
    long game =
        id(
            call(
                post("/api/v1/minigames"),
                owner,
                "{\"name\":\"Eco game\",\"url\":\"/game\",\"completionRules\":{\"minScore\":70}}",
                201));
    String body =
        questBody("MINIGAME", "Eco game quest")
            .replace("\"theme\":\"CHECKBOX\"", "\"theme\":\"MINIGAME\",\"minigameId\":" + game);
    long q = id(call(post("/api/v1/quests"), owner, body, 201));
    call(patch("/api/v1/quests/" + q + "/publish"), owner, null, 200);
    return q;
  }

  @ParameterizedTest
  @ValueSource(strings = {"finish", "cancel"})
  void minigameAttemptCannotBeChangedByAnotherAccount(String action) throws Exception {
    long q = minigameQuest(),
        a =
            id(
                call(
                    post("/api/v1/minigame-attempts"),
                    participant,
                    "{\"questId\":" + q + "}",
                    201));
    call(
        post("/api/v1/minigame-attempts/" + a + "/" + action),
        outsider,
        action.equals("finish") ? "{\"score\":80,\"metadata\":{}}" : null,
        403);
    call(post("/api/v1/minigame-attempts/" + a + "/cancel"), participant, null, 200);
  }

  @ParameterizedTest
  @org.junit.jupiter.params.provider.CsvSource({"69,false", "70,true"})
  void minigameRewardDependsOnScoreThreshold(int score, boolean earnsReward) throws Exception {
    long q = minigameQuest(),
        a =
            id(
                call(
                    post("/api/v1/minigame-attempts"),
                    participant,
                    "{\"questId\":" + q + "}",
                    201));
    String result =
        call(
            post("/api/v1/minigame-attempts/" + a + "/finish"),
            participant,
            "{\"score\":" + score + ",\"metadata\":{}}",
            200);
    assertEquals(earnsReward, JsonPath.read(result, "$.successful"));
    call(
        post("/api/v1/minigame-attempts/" + a + "/finish"),
        participant,
        "{\"score\":80,\"metadata\":{}}",
        422);
    int points =
        ((Number)
                JsonPath.read(
                    call(get("/api/v1/gamification/me/progress"), participant, null, 200),
                    "$.totalEcopoints"))
            .intValue();
    assertEquals(earnsReward ? 10 : 0, points);
  }
}
