package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalTopic;

@Converter
public class CommunityGoalTopicPersistenceConverter implements AttributeConverter<CommunityGoalTopic, String> {
    @Override
    public String convertToDatabaseColumn(CommunityGoalTopic attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CommunityGoalTopic convertToEntityAttribute(String value) {
        return value == null ? null : CommunityGoalTopic.fromValue(value);
    }
}
