package pe.greenminds.ecomind.community.infrastructure.storage.cloudinary;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class EventImageValidatorTests {
  private final EventImageValidator validator = new EventImageValidator();

  @Test
  void acceptsDecodablePngAndJpegImages() throws Exception {
    validator.validate(image("png", 3, 4));
    validator.validate(image("jpg", 5, 6));
  }

  @Test
  void rejectsBytesThatDoNotContainAnImage() {
    IllegalArgumentException error = assertThrows(
        IllegalArgumentException.class, () -> validator.validate(new byte[] {1, 2, 3, 4}));
    assertEquals("The file must be a valid JPEG or PNG image", error.getMessage());
  }

  @Test
  void rejectsFormatsOutsideJpegAndPng() throws Exception {
    IllegalArgumentException error = assertThrows(
        IllegalArgumentException.class, () -> validator.validate(image("gif", 2, 2)));
    assertEquals("Only JPEG and PNG images are supported", error.getMessage());
  }

  @Test
  void rejectsOversizedWidthAndHeight() throws Exception {
    IllegalArgumentException wide = assertThrows(
        IllegalArgumentException.class, () -> validator.validate(image("png", 4097, 1)));
    assertEquals("Image dimensions must not exceed 4096 x 4096", wide.getMessage());
    IllegalArgumentException tall = assertThrows(
        IllegalArgumentException.class, () -> validator.validate(image("png", 1, 4097)));
    assertEquals("Image dimensions must not exceed 4096 x 4096", tall.getMessage());
  }

  @Test
  void reportsTruncatedImageDataAsDamaged() throws Exception {
    byte[] valid = image("png", 4, 4);
    byte[] truncated = java.util.Arrays.copyOf(valid, 12);
    IllegalArgumentException error = assertThrows(
        IllegalArgumentException.class, () -> validator.validate(truncated));
    assertEquals("The image is invalid or damaged", error.getMessage());
  }

  private byte[] image(String format, int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    assertTrue(ImageIO.write(image, format, bytes));
    image.flush();
    return bytes.toByteArray();
  }
}
