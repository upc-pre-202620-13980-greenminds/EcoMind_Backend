package pe.greenminds.ecomind.community.infrastructure.persistence.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pe.greenminds.ecomind.community.domain.model.valueobjects.CommunityRole;

@Converter
public class CommunityRolePersistenceConverter implements AttributeConverter<CommunityRole, String> {
    @Override
    public String convertToDatabaseColumn(CommunityRole attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public CommunityRole convertToEntityAttribute(String value) {
        return value == null ? null : CommunityRole.valueOf(value.toUpperCase());
    }
}
