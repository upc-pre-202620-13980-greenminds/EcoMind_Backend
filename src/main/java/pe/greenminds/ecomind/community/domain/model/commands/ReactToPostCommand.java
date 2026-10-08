package pe.greenminds.ecomind.community.domain.model.commands;

public record ReactToPostCommand(Long postId,Long userId,String reactionType){}
