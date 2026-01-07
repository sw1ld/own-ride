package de.sw1ld;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter(autoApply = false)
public class PositionListJsonConverter implements AttributeConverter<List<Position>, String> {

  private static final TypeReference<List<Position>> TYPE = new TypeReference<>() {};

  private static final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public String convertToDatabaseColumn(List<Position> attribute) {
    if (attribute == null) {
      return "[]";
    }
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize positions to JSON", e);
    }
  }

  @Override
  public List<Position> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return List.of();
    }
    try {
      return objectMapper.readValue(dbData, TYPE);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to deserialize positions from JSON", e);
    }
  }
}
