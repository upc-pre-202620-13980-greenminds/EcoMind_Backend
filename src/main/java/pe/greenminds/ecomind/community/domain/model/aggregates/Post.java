package pe.greenminds.ecomind.community.domain.model.aggregates;
public record Post(Long id,Long communityId,Long authorId,String content,String postType,String imageUrl,Long relatedEventId){public Post{if(communityId==null||content==null||content.isBlank())throw new IllegalArgumentException("Community and post content are required");}}
