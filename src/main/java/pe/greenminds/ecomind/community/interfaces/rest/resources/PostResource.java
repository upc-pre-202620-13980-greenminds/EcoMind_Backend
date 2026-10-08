package pe.greenminds.ecomind.community.interfaces.rest.resources;
public record PostResource(Long id,Long community_id,Long author_id,String content,String post_type,String image_url,Long related_event_id,long likes){}
