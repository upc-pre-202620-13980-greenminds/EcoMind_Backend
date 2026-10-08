package pe.greenminds.ecomind.community.domain.model.commands;
public record CreatePostCommand(Long communityId,Long authorId,String content,String imageUrl){}
