package pe.greenminds.ecomind.community.interfaces.rest.transform;

import pe.greenminds.ecomind.community.domain.model.valueobjects.UploadedEventImage;
import pe.greenminds.ecomind.community.interfaces.rest.resources.EventImageResource;

public final class EventImageResourceFromEntityAssembler {

    private EventImageResourceFromEntityAssembler() {
    }

    public static EventImageResource toResourceFromEntity(
            UploadedEventImage image) {
        return new EventImageResource(
                image.publicId(),
                image.imageUrl());
    }
}