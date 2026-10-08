package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityType;

@Converter
public class CommunityTypePersistenceConverter implements AttributeConverter<CommunityType, String> {
    @Override
    public String convertToDatabaseColumn(CommunityType attribute) {
        return attribute == null ? null : attribute.value();
    }

    @Override
    public CommunityType convertToEntityAttribute(String value) {
        return value == null ? null : CommunityType.fromValue(value);
    }
}
