package pe.greenminds.ecomind.users.domain.model.valueobjects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class InvitationCodeTests {

  @Test
  void aCodeWithEightAllowedCharactersIsValid() {
    assertThat(new InvitationCode("K7MP4QXZ").value()).isEqualTo("K7MP4QXZ");
  }

  @ParameterizedTest
  @ValueSource(strings = {"K7MP4QX", "K7MP4QXZA", "k7mp4qxz", "K7MP4QX0", "K7MP4QXO", "K7MP4QX1",
      "K7MP4QXI", "K7MP 4QX"})
  void aCodeWithAWrongLengthOrForbiddenCharactersIsRejected(String value) {
    assertThatThrownBy(() -> new InvitationCode(value))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void aCodeIsRequired() {
    assertThatThrownBy(() -> new InvitationCode(null)).isInstanceOf(IllegalArgumentException.class);
  }

  // The code is random, so it is generated many times to check every result follows the format
  @RepeatedTest(50)
  void aGeneratedCodeAlwaysFollowsTheFormat() {
    assertThat(InvitationCode.generate().value()).matches(InvitationCode.FORMAT);
  }
}
