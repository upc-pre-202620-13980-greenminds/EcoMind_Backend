package pe.greenminds.ecomind.community.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendEventChatMessageResource(
        @NotBlank @Size(max = 2000) String text,
        @NotNull UUID client_id) {
}