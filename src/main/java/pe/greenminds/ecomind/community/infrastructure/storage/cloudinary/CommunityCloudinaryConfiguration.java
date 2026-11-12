package pe.greenminds.ecomind.community.infrastructure.storage.cloudinary;

import com.cloudinary.Cloudinary;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommunityCloudinaryConfiguration {

    @Bean
    public Cloudinary communityCloudinary(
            @Value("${community.cloudinary.cloud-name}") String cloudName,
            @Value("${community.cloudinary.api-key}") String apiKey,
            @Value("${community.cloudinary.api-secret}") String apiSecret) {

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IllegalStateException(
                    "Community Cloudinary credentials must not be blank");
        }

        return new Cloudinary(Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }
}   