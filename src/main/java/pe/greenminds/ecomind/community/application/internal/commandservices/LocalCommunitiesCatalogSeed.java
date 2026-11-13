package pe.greenminds.ecomind.community.application.internal.commandservices;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.greenminds.ecomind.community.application.commandservices.CommunityCommandService;
import pe.greenminds.ecomind.community.domain.model.commands.CreateLocalCommunityCommand;
import pe.greenminds.ecomind.community.domain.model.queries.SearchCommunitiesQuery;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository;

@Component
@ConditionalOnProperty(
        name = "community.local-catalog.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class LocalCommunitiesCatalogSeed implements ApplicationRunner {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(LocalCommunitiesCatalogSeed.class);

    private static final List<String> LOCALITIES = List.of(
            "Lima",
            "Arequipa",
            "Trujillo",
            "Chiclayo",
            "Piura",
            "Cusco",
            "Iquitos",
            "Huancayo",
            "Tacna",
            "Puno",
            "Juliaca",
            "Chimbote",
            "Ica",
            "Cajamarca",
            "Huánuco",
            "Huaraz",
            "Ayacucho",
            "Tarapoto",
            "Pucallpa",
            "Tumbes");

    private final CommunityRepository communities;
    private final CommunityCommandService commands;

    public LocalCommunitiesCatalogSeed(
            CommunityRepository communities,
            CommunityCommandService commands) {
        this.communities = communities;
        this.commands = commands;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        communities.lockLocalCatalog();

        Set<String> existingLocalities = new HashSet<>();

        communities.search(new SearchCommunitiesQuery(CommunityType.LOCAL, null))
                .forEach(community ->
                        existingLocalities.add(normalize(community.getLocality())));

        int created = 0;

        for (String locality : LOCALITIES) {
            String key = normalize(locality);

            if (existingLocalities.contains(key)) {
                continue;
            }

            var command = new CreateLocalCommunityCommand(
                    locality + " Green Community",
                    "Local community in " + locality
                            + " to share sustainable actions, participate in events"
                            + " and complete community goals.",
                    locality,
                    null,
                    null);

            commands.handle(command)
                    .toOptional()
                    .orElseThrow(() -> new IllegalStateException(
                            "Could not initialize the local community for " + locality));

            existingLocalities.add(key);
            created++;
        }

        LOGGER.info(
                "Local community catalog initialized: {} created, {} localities checked",
                created,
                LOCALITIES.size());
    }

    private String normalize(String locality) {
        return locality.trim().toLowerCase(Locale.ROOT);
    }
}