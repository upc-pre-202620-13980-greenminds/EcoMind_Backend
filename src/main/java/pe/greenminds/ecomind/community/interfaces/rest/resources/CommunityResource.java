package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Community")
public record CommunityResource(Long id,String name,String description,String type,String topic,String locality,Integer member_limit,String icon_url,Long created_by) {}
