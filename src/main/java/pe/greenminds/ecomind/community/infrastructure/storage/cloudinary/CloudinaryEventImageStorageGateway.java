package pe.greenminds.ecomind.community.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pe.greenminds.ecomind.community.application.outboundservices.EventImageStorageGateway;
import pe.greenminds.ecomind.community.domain.model.valueobjects.UploadedEventImage;

@Component
public class CloudinaryEventImageStorageGateway
        implements EventImageStorageGateway {

    private final Cloudinary cloudinary;
    private final EventImageValidator validator;

    public CloudinaryEventImageStorageGateway(
            @Qualifier("communityCloudinary") Cloudinary cloudinary,
            EventImageValidator validator) {
        this.cloudinary = cloudinary;
        this.validator = validator;
    }

    @Override
    public UploadedEventImage upload(
            Long authorId,
            byte[] content) throws IOException {

        validator.validate(content);

        String publicId = "ecomind/community/events/"
                + authorId + "/" + UUID.randomUUID();

        Map<?, ?> result = cloudinary.uploader().upload(
                content,
                Map.of(
                        "resource_type", "image",
                        "public_id", publicId,
                        "overwrite", false));

        Object storedPublicId = result.get("public_id");
        Object secureUrl = result.get("secure_url");

        if (!(storedPublicId instanceof String id)
                || id.isBlank()
                || !(secureUrl instanceof String url)
                || !url.startsWith("https://")) {
            throw new IOException(
                    "The image storage returned an invalid response");
        }

        return new UploadedEventImage(id, url);
    }
}