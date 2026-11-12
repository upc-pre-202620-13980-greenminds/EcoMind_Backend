package pe.greenminds.ecomind.community.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.greenminds.ecomind.community.application.commandservices.EventImageCommandService;
import pe.greenminds.ecomind.community.application.outboundservices.CommunityActorGateway;
import pe.greenminds.ecomind.community.application.outboundservices.EventImageStorageGateway;
import pe.greenminds.ecomind.community.domain.model.commands.UploadEventImageCommand;
import pe.greenminds.ecomind.community.domain.model.valueobjects.UploadedEventImage;
import pe.greenminds.ecomind.shared.application.result.ApplicationError;
import pe.greenminds.ecomind.shared.application.result.Result;

@Service
public class EventImageCommandServiceImpl implements EventImageCommandService {

    private static final int MAX_IMAGE_BYTES = 5 * 1024 * 1024;

    private final CommunityActorGateway actors;
    private final EventImageStorageGateway storage;

    public EventImageCommandServiceImpl(
            CommunityActorGateway actors,
            EventImageStorageGateway storage) {
        this.actors = actors;
        this.storage = storage;
    }

    @Override
    public Result<UploadedEventImage, ApplicationError> handle(
            UploadEventImageCommand command) {

        try {
            actors.requireParent(command.authorId());

            byte[] content = command.content();

            if (content == null || content.length == 0) {
                return Result.failure(ApplicationError.validationError(
                        "Event image", "An image is required"));
            }

            if (content.length > MAX_IMAGE_BYTES) {
                return Result.failure(ApplicationError.validationError(
                        "Event image", "The image must not exceed 5 MiB"));
            }

            return Result.success(
                    storage.upload(command.authorId(), content));

        } catch (SecurityException exception) {
            return Result.failure(ApplicationError.forbidden(
                    "EVENT_CREATOR_NOT_PARENT",
                    "Only parent users may upload event images"));

        } catch (IllegalArgumentException exception) {
            return Result.failure(ApplicationError.validationError(
                    "Event image", exception.getMessage()));

        } catch (Exception exception) {
            return Result.failure(ApplicationError.unexpected(
                    "Event image upload",
                    "The image could not be uploaded. Please try again"));
        }
    }
}