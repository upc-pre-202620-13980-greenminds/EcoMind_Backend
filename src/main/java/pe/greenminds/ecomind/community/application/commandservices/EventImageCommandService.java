package pe.greenminds.ecomind.community.application.commandservices;

import pe.greenminds.ecomind.community.domain.model.commands.UploadEventImageCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.UploadedEventImage;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

public interface EventImageCommandService {
    Result<UploadedEventImage, ApplicationError> handle(UploadEventImageCommand command);
}