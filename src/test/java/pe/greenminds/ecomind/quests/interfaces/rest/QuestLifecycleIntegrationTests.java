package pe.greenminds.ecomind.quests.interfaces.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
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
import pe.greenminds.ecomind.quests.application.internal.services.DailyQuestLifecycleService;
import pe.greenminds.ecomind.quests.application.internal.services.QuestCatalogSeed;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestPublicationStatus;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;
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
  @Autowired QuestCatalogSeed questCatalogSeed;
  @Autowired QuestRepository quests;
  @Autowired ActivityRepository activities;
  @Autowired DailyQuestLifecycleService dailyQuests;
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

  @Test
  void startupCatalogHasThreeDailyQuestsAndAutomaticallyAssignsOne() throws Exception {
    var published = quests.findByPublicationStatus(QuestPublicationStatus.PUBLISHED);
    var writingTitles = Map.of(
        Category.ENERGY, "My Energy-Saving Habit",
        Category.RECYCLE, "Give It a Second Life",
        Category.WATER, "My Water-Saving Plan");
    for (var category : Category.values()) {
      assertTrue(published.stream().filter(quest -> quest.getCategory() == category)
          .filter(quest -> quest.getType() == QuestType.ACTIVITIES || quest.getType() == QuestType.COLLABORATIVE)
          .count() >= 4);
      assertTrue(published.stream().anyMatch(quest -> quest.getCategory() == category
          && quest.getType() == QuestType.COLLABORATIVE));
      var writingQuest = published.stream()
          .filter(quest -> quest.getCategory() == category && quest.getType() == QuestType.ACTIVITIES
              && quest.getTitle().equals(writingTitles.get(category)))
          .findFirst().orElseThrow();
      assertEquals(Theme.WRITE, writingQuest.getTheme());
      var writingSteps = activities.findByQuestsIdOrderByOrderAsc(writingQuest.getId());
      assertEquals(1, writingSteps.size());
      assertEquals(ActivityType.WRITE, writingSteps.getFirst().getActivityType());
      assertEquals(15, writingSteps.getFirst().getActivityConfiguration().get("minCharacters"));
    }
    assertTrue(published.stream().filter(quest -> quest.getType() == QuestType.FAMILY).count() >= 4);
    var collectWaste = published.stream()
        .filter(quest -> quest.getType() == QuestType.MINIGAME && "Collect Waste".equals(quest.getTitle()))
        .findFirst().orElseThrow();
    assertEquals(Category.RECYCLE, collectWaste.getCategory());
    var game = call(get("/api/v1/minigames/" + collectWaste.getMinigameId()), participant, null, 200);
    assertEquals("/games/collect-waste", JsonPath.read(game, "$.url"));
    assertEquals(10, JsonPath.<Integer>read(game, "$.completionRules.minScore").intValue());

    var questCount = quests.findAll().size();
    questCatalogSeed.run(new DefaultApplicationArguments());
    assertEquals(questCount, quests.findAll().size());

    var today = dailyQuests.today();
    var dailies = published.stream()
        .filter(quest -> quest.getType() == QuestType.DAILY_QUEST && today.equals(quest.getAssignedDate()))
        .toList();
    assertEquals(3, dailies.size());
    assertEquals(Set.of(Category.values()), dailies.stream().map(Quest::getCategory).collect(Collectors.toSet()));

    var status = "/api/v1/quest-users/me/status/IN_PROGRESS";
    List<Integer> assignedIds = JsonPath.read(call(get(status), participant, null, 200), "$[*].questId");
    assertEquals(1, assignedIds.size());
    var assignedId = assignedIds.getFirst().longValue();
    assertTrue(dailies.stream().anyMatch(quest -> quest.getId() == assignedId));
    List<Integer> repeated = JsonPath.read(call(get(status), participant, null, 200), "$[*].questId");
    assertEquals(assignedIds, repeated);
  }

  @Test
  @org.springframework.transaction.annotation.Transactional
  void startupCatalogMigratesTheLegacyMinigameInPlace() throws Exception {
    var gameId = id(call(post("/api/v1/minigames"), owner, """
        {"name":"Eco Collector","description":"Collect energy to reach the target score.",
         "url":"/quests/minigames/simple-score","completionRules":{"minScore":800}}
        """, 201));
    var questId = id(call(post("/api/v1/quests"), owner, """
        {"title":"Eco Collector",
         "description":"Play the energy collection minigame and reach its target score.",
         "category":"ENERGY","type":"MINIGAME","theme":"MINIGAME","age":8,"time":5,
         "gemReward":3,"ecopoints":30,"minigameId":%d}
        """.formatted(gameId), 201));
    call(patch("/api/v1/quests/" + questId + "/publish"), owner, null, 200);
    var questCount = quests.findAll().size();

    questCatalogSeed.run(new DefaultApplicationArguments());
    questCatalogSeed.run(new DefaultApplicationArguments());

    assertEquals(questCount, quests.findAll().size());
    var migrated = quests.findById(questId).orElseThrow();
    assertEquals(gameId, migrated.getMinigameId().longValue());
    assertEquals("Collect Waste", migrated.getTitle());
    assertEquals(Category.RECYCLE, migrated.getCategory());
    assertEquals(QuestPublicationStatus.PUBLISHED, migrated.getPublicationStatus());
    var game = call(get("/api/v1/minigames/" + gameId), participant, null, 200);
    assertEquals("/games/collect-waste", JsonPath.read(game, "$.url"));
    assertEquals(10, JsonPath.<Integer>read(game, "$.completionRules.minScore").intValue());
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

  @ParameterizedTest
  @ValueSource(ints = {17, 24})
  void guidedQuestSavesWrittenResponseAndCountsItOnceAlongsideCheckbox(int minCharacters)
      throws Exception {
    String answer = "Apagaré la luz al salir.".substring(0, minCharacters);
    String questJson = questBody("ACTIVITIES", "Ahorra energía").replace("CHECKBOX", "WRITE");
    long questId = id(call(post("/api/v1/quests"), owner, questJson, 201));
    long writeActivityId = id(call(post("/api/v1/activities"), owner, """
        {"questId":%d,"description":"¿Qué harás para ahorrar energía?","order":1,"type":"WRITE",
         "activityConfiguration":{"minCharacters":12}}
        """.formatted(questId), 201));
    long checkboxActivityId = id(call(post("/api/v1/activities"), owner, """
        {"questId":%d,"description":"Apaga la luz de un cuarto vacío.","order":2,"type":"CHECKBOX"}
        """.formatted(questId), 201));
    for (String config : new String[] {"{}", "{\"minCharacters\":0}", "{\"minCharacters\":1.5}",
        "{\"minCharacters\":\"24\"}"}) {
      call(post("/api/v1/activities"), owner, """
          {"questId":%d,"description":"Respuesta inválida","order":1,"type":"WRITE",
           "activityConfiguration":%s}
          """.formatted(questId, config), 400);
    }
    String activityUrl = "/api/v1/activities/" + writeActivityId;
    call(put(activityUrl), owner, """
        {"description":"¿Qué harás para ahorrar energía?","order":2,"type":"WRITE",
         "activityConfiguration":{"minCharacters":-1}}
        """, 400);
    assertEquals(2, (int) JsonPath.read(call(get("/api/v1/activities/" + checkboxActivityId),
        owner, null, 200), "$.order"));
    call(put(activityUrl), owner, """
        {"description":"¿Qué harás para ahorrar energía?","order":1,"type":"WRITE",
         "activityConfiguration":{"minCharacters":%d}}
        """.formatted(minCharacters), 200);
    call(patch("/api/v1/quests/" + questId + "/publish"), owner, null, 200);
    assertEquals("WRITE", JsonPath.read(call(get("/api/v1/quests/" + questId), participant,
        null, 200), "$.theme"));
    assertEquals(java.util.List.of("WRITE"), JsonPath.read(call(get("/api/v1/quests"),
        participant, null, 200), "$[?(@.id == " + questId + ")].theme"));

    long assignment = id(call(post("/api/v1/quest-users"), participant,
        "{\"questId\":" + questId + "}", 201));
    String questUserUrl = "/api/v1/quest-users/" + assignment;
    String activitiesUrl = "/api/v1/activity-users/quest-user/" + assignment;
    String writeLookup = activitiesUrl + "/activity/" + writeActivityId;
    String beforeSubmit = call(get(writeLookup), participant, null, 200);
    assertFalse(beforeSubmit.contains("\"responseText\""));
    assertEquals(minCharacters,
        (int) JsonPath.read(beforeSubmit, "$.activityConfiguration.minCharacters"));
    String writeUrl = "/api/v1/activity-users/" + id(beforeSubmit);
    String checkbox = call(get(activitiesUrl + "/activity/" + checkboxActivityId), participant,
        null, 200);
    String checkboxSubmitUrl = "/api/v1/activity-users/" + id(checkbox) + "/submit";

    for (String data : new String[] {"{}", "{\"text\":null}", "{\"text\":123}", "{\"text\":\" \\t \"}"}) {
      call(post(writeUrl + "/submit"), participant, "{\"data\":" + data + "}", 400);
    }
    call(post(writeUrl + "/submit"), participant,
        """
        {"data":{"text":"  %s  "}}
        """.formatted(answer.substring(0, minCharacters - 1)), 400);
    String pending = call(get(writeUrl), participant, null, 200);
    assertEquals(0.0, ((Number) JsonPath.read(pending, "$.progress")).doubleValue());
    assertFalse(pending.contains("\"responseText\""));

    String submitted = call(post(writeUrl + "/submit"), participant,
        """
        {"data":{"text":"  %s  "}}
        """.formatted(answer), 200);
    assertEquals(answer, JsonPath.read(submitted, "$.responseText"));
    assertEquals(100.0, ((Number) JsonPath.read(submitted, "$.progress")).doubleValue());
    String retried = call(post(writeUrl + "/submit"), participant,
        """
        {"data":{"text":"%s"}}
        """.formatted(answer), 200);
    assertEquals(JsonPath.<String>read(submitted, "$.endDate"),
        JsonPath.<String>read(retried, "$.endDate"));
    for (String url : new String[] {writeUrl, writeLookup}) {
      assertEquals(answer,
          JsonPath.read(call(get(url), participant, null, 200), "$.responseText"));
    }
    assertEquals(java.util.List.of(answer),
        JsonPath.read(call(get(activitiesUrl), participant, null, 200), "$[*].responseText"));
    call(get(writeUrl), outsider, null, 403);

    String partial = call(get(questUserUrl), participant, null, 200);
    assertEquals(50.0, ((Number) JsonPath.read(partial, "$.progress")).doubleValue());
    assertEquals("IN_PROGRESS", JsonPath.read(partial, "$.status"));
    call(post(questUserUrl + "/complete"), participant, null, 422);
    call(post(checkboxSubmitUrl), participant, "{\"data\":{\"checked\":false}}", 200);
    String checked = call(post(checkboxSubmitUrl), participant,
        "{\"data\":{\"checked\":true}}", 200);
    assertEquals(100.0, ((Number) JsonPath.read(checked, "$.progress")).doubleValue());
    assertFalse(checked.contains("\"responseText\""));
    assertEquals("READY_TO_COMPLETE",
        JsonPath.read(call(get(questUserUrl), participant, null, 200), "$.status"));
    assertEquals("COMPLETED",
        JsonPath.read(call(post(questUserUrl + "/complete"), participant, null, 200), "$.status"));
    call(post(writeUrl + "/submit"), participant, "{\"data\":{\"text\":\"Otro texto\"}}", 422);
    assertEquals(answer,
        JsonPath.read(call(get(writeUrl), participant, null, 200), "$.responseText"));
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
    call(patch("/api/v1/collaborative-quest-members/" + m + "/remove"), outsider, null, 403);
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
