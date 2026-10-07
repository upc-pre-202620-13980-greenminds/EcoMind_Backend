package pe.greenminds.ecomind.quests.interfaces.rest.transform;

import pe.greenminds.ecomind.quests.application.queryservices.QuestUserVersionStatus;
import pe.greenminds.ecomind.quests.interfaces.rest.resources.QuestUserVersionStatusResource;

public final class QuestUserVersionStatusResourceAssembler {
    private QuestUserVersionStatusResourceAssembler() {
    }

    public static QuestUserVersionStatusResource toResource(
            QuestUserVersionStatus versionStatus
    ) {
        return new QuestUserVersionStatusResource(
                versionStatus.questUserId(),
                versionStatus.upToDate(),
                versionStatus.missingActivityIds(),
                versionStatus.outdatedActivityIds()
        );
    }
}
