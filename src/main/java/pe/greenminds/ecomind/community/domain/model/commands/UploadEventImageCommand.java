package pe.greenminds.ecomind.community.domain.model.commands;

public record UploadEventImageCommand(
        Long authorId,
        byte[] content) {
}