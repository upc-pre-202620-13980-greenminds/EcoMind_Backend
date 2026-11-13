package pe.greenminds.ecomind.community.infrastructure.storage.cloudinary;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.stereotype.Component;

@Component
public class EventImageValidator {

    private static final Set<String> ALLOWED_FORMATS =
            Set.of("jpeg", "jpg", "png");

    private static final int MAX_DIMENSION = 4096;

    public void validate(byte[] content) {
        try (var input = new MemoryCacheImageInputStream(
                new ByteArrayInputStream(content))) {

            var readers = ImageIO.getImageReaders(input);

            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "The file must be a valid JPEG or PNG image");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(input, true, true);

                String format = reader.getFormatName()
                        .toLowerCase(Locale.ROOT);

                if (!ALLOWED_FORMATS.contains(format)) {
                    throw new IllegalArgumentException(
                            "Only JPEG and PNG images are supported");
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width < 1 || height < 1
                        || width > MAX_DIMENSION
                        || height > MAX_DIMENSION) {
                    throw new IllegalArgumentException(
                            "Image dimensions must not exceed 4096 x 4096");
                }

                var image = reader.read(0);

                if (image == null) {
                    throw new IllegalArgumentException(
                            "The image could not be decoded");
                }

                image.flush();
            } finally {
                reader.dispose();
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "The image is invalid or damaged");
        }
    }
}