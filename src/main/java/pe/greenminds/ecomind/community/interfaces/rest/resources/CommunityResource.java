package pe.greenminds.ecomind.community.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.Schema;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;

@Schema(name="Community")
public record CommunityResource(Long id,String name,String description,CommunityType type,String topic,String locality,Integer member_limit,String icon_url,Long created_by) {}
