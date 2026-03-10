package fr.beacrea.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonNodeConverter implements AttributeConverter<JsonNode, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(JsonNode pAttribute) {
        if (pAttribute == null) return null;
        try {
            return MAPPER.writeValueAsString(pAttribute);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JsonNode to String", e);
        }
    }

    @Override
    public JsonNode convertToEntityAttribute(String pDbData) {
        if (pDbData == null) return null;
        try {
            return MAPPER.readTree(pDbData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize String to JsonNode", e);
        }
    }
}
