package pe.greenminds.ecomind.community.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record CreatePostResource(@NotNull Long community_id,@NotBlank String content,String image_url,@NotNull Long author_id){}
