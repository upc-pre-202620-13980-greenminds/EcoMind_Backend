package pe.greenminds.ecomind.community.domain.model.commands;

public record CreateTopicCommunityCommand(
        String name, String description, String topic, Integer memberLimit, String iconUrl, Long userId) {}
