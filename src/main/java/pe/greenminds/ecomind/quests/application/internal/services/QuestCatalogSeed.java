package pe.greenminds.ecomind.quests.application.internal.services;

import jakarta.transaction.Transactional;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Activity;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Minigame;
import pe.greenminds.ecomind.quests.domain.model.aggregates.Quest;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.ActivityType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Category;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Reward;
import pe.greenminds.ecomind.quests.domain.model.valueobjects.Theme;
import pe.greenminds.ecomind.quests.domain.repositories.ActivityRepository;
import pe.greenminds.ecomind.quests.domain.repositories.MinigameRepository;
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static pe.greenminds.ecomind.quests.domain.model.valueobjects.Category.ENERGY;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.Category.RECYCLE;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.Category.WATER;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.ACTIVITIES;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.COLLABORATIVE;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.DAILY_QUEST;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.FAMILY;
import static pe.greenminds.ecomind.quests.domain.model.valueobjects.QuestType.MINIGAME;

@Component
public class QuestCatalogSeed implements ApplicationRunner {
    private static final String MINIGAME_PATH = "/games/collect-waste";
    private static final String MINIGAME_TITLE = "Collect Waste";
    private static final String MINIGAME_DESCRIPTION = "Collect waste to reach the target score.";
    private static final int MINIGAME_TARGET = 10;
    private static final String LEGACY_MINIGAME_PATH = "/quests/minigames/simple-score";
    private static final String LEGACY_MINIGAME_TITLE = "Eco Collector";
    private static final String LEGACY_QUEST_DESCRIPTION =
            "Play the energy collection minigame and reach its target score.";
    private static final List<StarterQuest> CATALOG = List.of(
            new StarterQuest(ENERGY, ACTIVITIES, "The Daylight Challenge",
                    "Use natural light for one daytime activity.", 15,
                    "Open the curtains or blinds in a room you use during the day.",
                    "Read, draw, or do homework there for 15 minutes with the lights off."),
            new StarterQuest(ENERGY, ACTIVITIES, "Fridge Door Plan",
                    "Practice keeping cold air inside the refrigerator.", 5,
                    "Decide what you need before opening the fridge.",
                    "Take everything you need in one trip and check that the door closes fully."),
            new StarterQuest(ENERGY, ACTIVITIES, "Power Down Your Screen",
                    "Save electricity after your next screen session.", 5,
                    "Save your work or game before you leave the device.",
                    "Turn off the screen or shut down the device if you are done using it for the day."),
            new StarterQuest(ENERGY, COLLABORATIVE, "Lights-Out Team",
                    "Work with your team to find where lighting energy can be saved.", 15,
                    "Have each teammate check a different room for lights left on in empty spaces.",
                    "Share your findings and agree on one reminder everyone can use."),
            new StarterQuest(RECYCLE, ACTIVITIES, "Paper Rescue",
                    "Give clean, single-sided paper another use.", 10,
                    "Gather three sheets that are blank on one side.",
                    "Use the blank sides for notes or sketches and put the rest in a reuse pile."),
            new StarterQuest(RECYCLE, ACTIVITIES, "Read the Recycling Rules",
                    "Find out which common items your local recycling program accepts.", 10,
                    "Check the recycling instructions on a local bin or in your local collection guide.",
                    "Pick one clean household item and sort it according to those instructions."),
            new StarterQuest(RECYCLE, ACTIVITIES, "A Box Gets a Second Job",
                    "Turn a cardboard box into something you can use again.", 15,
                    "Find a clean cardboard box that would otherwise be thrown away.",
                    "Use the box to organize books, school supplies, or toys."),
            new StarterQuest(RECYCLE, COLLABORATIVE, "Sorting Squad",
                    "Create a shared sorting guide with your teammates.", 20,
                    "Each teammate checks where one common packaging item belongs.",
                    "Compare your results and write a guide with at least three examples."),
            new StarterQuest(WATER, ACTIVITIES, "A Shorter Shower",
                    "Use a timer to make your shower a little shorter.", 10,
                    "Set a timer for one minute less than your usual shower.",
                    "Finish before the timer goes off while rinsing thoroughly."),
            new StarterQuest(WATER, ACTIVITIES, "A Second Use for Water",
                    "Reuse plain water from rinsing produce.", 10,
                    "Collect water from rinsing fruit or vegetables in a clean bowl.",
                    "Use that water on a plant that needs watering."),
            new StarterQuest(WATER, ACTIVITIES, "Spot the Drip",
                    "Check whether a faucet keeps dripping after you close it.", 5,
                    "Turn a faucet off completely and watch it for one minute.",
                    "Share what you found with an adult so they can follow up if it drips."),
            new StarterQuest(WATER, COLLABORATIVE, "Water-Saving Swap",
                    "Trade water-saving ideas with your teammates and try one out.", 15,
                    "Have each teammate share one practical way to use less water.",
                    "Choose one idea as a team and have everyone try it today."),
            new StarterQuest(ENERGY, ACTIVITIES, "My Energy-Saving Habit",
                    "Make a plan to save electricity at home.", 5, ActivityType.WRITE,
                    "In one sentence, describe a device you can turn off and when you will do it."),
            new StarterQuest(RECYCLE, ACTIVITIES, "Give It a Second Life",
                    "Think of a new use for something you would usually throw away.", 5, ActivityType.WRITE,
                    "Name one item you can reuse and explain how you would use it again."),
            new StarterQuest(WATER, ACTIVITIES, "My Water-Saving Plan",
                    "Pick one everyday habit you can change to use less water.", 5, ActivityType.WRITE,
                    "Write one way you can save water at home and when you will try it."),
            new StarterQuest(ENERGY, DAILY_QUEST, "Daily Lights Check",
                    "Save energy the next time you leave a room.", 5,
                    "Switch off the lights when you leave an empty room."),
            new StarterQuest(RECYCLE, DAILY_QUEST, "Daily Sorting Check",
                    "Sort one item using your local recycling rules.", 5,
                    "Check whether a clean piece of packaging is accepted and put it in the correct bin."),
            new StarterQuest(WATER, DAILY_QUEST, "Daily Tap Check",
                    "Save water while brushing your teeth today.", 5,
                    "Turn off the tap while brushing and open it again when you need to rinse."),
            new StarterQuest(ENERGY, FAMILY, "Family Energy Walk",
                    "Look for simple ways to save electricity together at home.", 20,
                    "Walk through your home together and look for lights or screens left on.",
                    "Agree on two energy-saving habits to practice this week."),
            new StarterQuest(RECYCLE, FAMILY, "Build a Family Sorting Station",
                    "Set up a place where everyone can sort household waste.", 25,
                    "Choose a spot for recyclables and label the containers using local collection rules.",
                    "Have each family member sort one item using the new labels."),
            new StarterQuest(WATER, FAMILY, "Family Water Detective Day",
                    "Check household taps for leaks and choose a water-saving habit.", 20,
                    "Check two taps together and tell an adult about any leaks you find.",
                    "Pick one change everyone can make to save water this week."),
            new StarterQuest(RECYCLE, FAMILY, "Pack a Waste-Free Snack",
                    "Plan a family snack using containers you can wash and reuse.", 15,
                    "Prepare snacks and drinks together using reusable containers.",
                    "Wash the containers and put them away for the next outing.")
    );

    private final QuestRepository quests;
    private final ActivityRepository activities;
    private final MinigameRepository minigames;
    private final DailyQuestLifecycleService dailyQuests;

    public QuestCatalogSeed(
            QuestRepository quests,
            ActivityRepository activities,
            MinigameRepository minigames,
            DailyQuestLifecycleService dailyQuests
    ) {
        this.quests = quests;
        this.activities = activities;
        this.minigames = minigames;
        this.dailyQuests = dailyQuests;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        quests.lockCatalog();
        var today = dailyQuests.today();
        for (var entry : CATALOG) {
            if (!quests.existsByTitleAndTypeAndCategory(entry.title(), entry.type(), entry.category())) {
                seedQuest(entry, null, entry.type() == DAILY_QUEST ? today : null);
            }
        }
        seedMinigameQuest();
    }

    private void seedMinigameQuest() {
        migrateLegacyMinigameQuest();
        if (quests.existsByTitleAndTypeAndCategory(MINIGAME_TITLE, MINIGAME, RECYCLE)) {
            return;
        }
        var game = minigames.findAll().stream()
                .filter(existing -> MINIGAME_PATH.equals(existing.getUrl()))
                .findFirst()
                .orElseGet(() -> saveCollectWaste(null));
        if (!quests.existsPublishedMinigameQuest(game.getId())) {
            seedQuest(new StarterQuest(RECYCLE, MINIGAME, MINIGAME_TITLE,
                    MINIGAME_DESCRIPTION, 5), game.getId(), null);
        }
    }

    private void migrateLegacyMinigameQuest() {
        var legacyQuests = quests.search(LEGACY_MINIGAME_TITLE, ENERGY, MINIGAME, null, Theme.MINIGAME);
        for (var quest : legacyQuests) {
            if (!LEGACY_MINIGAME_TITLE.equals(quest.getTitle())
                    || !LEGACY_QUEST_DESCRIPTION.equals(quest.getDescription())
                    || quest.getMinigameId() == null) {
                continue;
            }
            var game = minigames.findById(quest.getMinigameId());
            if (game.isEmpty() || !game.get().getUrl().endsWith(LEGACY_MINIGAME_PATH)) {
                continue;
            }
            // Preserve the IDs referenced by attempts and quest assignments.
            saveCollectWaste(quest.getMinigameId());
            quests.save(new Quest(quest.getId(), quest.getMinigameId(), MINIGAME_TITLE, RECYCLE,
                    MINIGAME_DESCRIPTION, MINIGAME, quest.getAge(), quest.getReward(),
                    quest.getTime(), quest.getImage(), quest.getTheme(), quest.getAssignedDate(),
                    quest.getVersionGroupId(), quest.getVersionNumber(), quest.getPublicationStatus()));
        }
    }

    private Minigame saveCollectWaste(Long id) {
        return minigames.save(new Minigame(id, MINIGAME_TITLE, MINIGAME_DESCRIPTION,
                MINIGAME_PATH, Map.of("minScore", MINIGAME_TARGET)));
    }

    private void seedQuest(StarterQuest entry, Long minigameId, LocalDate assignedDate) {
        var theme = switch (entry.type()) {
            case COLLABORATIVE -> Theme.COLLABORATIVE;
            case MINIGAME -> Theme.MINIGAME;
            case ACTIVITIES -> entry.activityType() == ActivityType.WRITE ? Theme.WRITE : Theme.CHECKBOX;
            default -> Theme.CHECKBOX;
        };
        var reward = switch (entry.type()) {
            case DAILY_QUEST -> new Reward(1, 10);
            case COLLABORATIVE, MINIGAME -> new Reward(3, 30);
            case FAMILY -> new Reward(0, 40);
            case ACTIVITIES -> new Reward(2, 20);
        };
        var quest = new Quest(minigameId, entry.title(), entry.category(), entry.description(),
                entry.type(), 8, reward, entry.minutes(), null, theme, assignedDate);
        quest.publish();
        var saved = quests.save(quest);
        quests.save(saved);
        Map<String, Object> configuration = entry.activityType() == ActivityType.WRITE
                ? Map.of("minCharacters", 15) : Map.of();
        for (int index = 0; index < entry.steps().length; index++) {
            activities.save(new Activity(saved.getId(), entry.steps()[index], index + 1,
                    entry.activityType(), configuration, null));
        }
    }

    private record StarterQuest(
            Category category, QuestType type, String title, String description, int minutes,
            ActivityType activityType, String... steps
    ) {
        StarterQuest(Category category, QuestType type, String title, String description, int minutes, String... steps) {
            this(category, type, title, description, minutes, ActivityType.CHECKBOX, steps);
        }
    }
}
