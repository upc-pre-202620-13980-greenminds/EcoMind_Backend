package pe.greenminds.ecomind.community.application.outboundservices;

import java.io.IOException;
import pe.greenminds.ecomind.community.domain.model.valueobjects.UploadedEventImage;

public interface EventImageStorageGateway {

    UploadedEventImage upload(Long authorId, byte[] content) throws IOException;
}