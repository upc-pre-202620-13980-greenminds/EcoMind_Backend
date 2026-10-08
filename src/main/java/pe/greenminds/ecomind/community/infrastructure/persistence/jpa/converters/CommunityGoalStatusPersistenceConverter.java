package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityGoalStatus;

@Converter
public class CommunityGoalStatusPersistenceConverter implements AttributeConverter<CommunityGoalStatus, String> {
    @Override
    public String convertToDatabaseColumn(CommunityGoalStatus status) {
        return status == null ? null : status.value();
    }

    @Override
    public CommunityGoalStatus convertToEntityAttribute(String value) {
        return value == null ? null : CommunityGoalStatus.fromValue(value);
    }
}
