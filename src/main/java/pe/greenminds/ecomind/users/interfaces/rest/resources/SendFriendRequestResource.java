package pe.greenminds.ecomind.users.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import pe.greenminds.ecomind.users.domain.model.valueobjects.InvitationCode;

@Schema(description = "User who will receive the friend request: by id or by invitation code")
public record SendFriendRequestResource(
    @Schema(description = "Id of the receiver; leave it out when sending a code", example = "8",
        nullable = true)
    @Positive
    Long receiverId,
    @Schema(description = "Invitation code of the receiver; leave it out when sending an id",
        example = "K7MP4QXZ", nullable = true)
    @Pattern(regexp = InvitationCode.FORMAT, message = "{users.validation.invitation-code}")
    String invitationCode) {

  /** Exactly one way to find the receiver must be sent. */
  @JsonIgnore
  @Schema(hidden = true)
  @AssertTrue(message = "{users.validation.friend-request-receiver}")
  public boolean isSingleReceiver() {
    return (receiverId == null) != (invitationCode == null);
  }
}
