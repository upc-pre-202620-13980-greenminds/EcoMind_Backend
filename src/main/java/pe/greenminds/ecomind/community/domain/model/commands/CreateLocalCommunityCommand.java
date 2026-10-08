package pe.greenminds.ecomind.community.domain.model.commands;

public record CreateLocalCommunityCommand(
        String name, String description, String locality, String iconUrl, Long userId) {}
