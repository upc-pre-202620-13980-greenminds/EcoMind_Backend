package pe.greenminds.ecomind.community.domain.model.commands;

public record UpdatePostReactionTypeCommand(Long postId, Long userId, String reactionType) {}
