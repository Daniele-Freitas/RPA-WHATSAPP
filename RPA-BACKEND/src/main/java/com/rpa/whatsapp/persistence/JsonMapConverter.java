package com.rpa.whatsapp.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class JsonMapConverter implements AttributeConverter<Map<String, String>, String> {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

  @Override
  public String convertToDatabaseColumn(Map<String, String> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return null;
    }

    try {
      return OBJECT_MAPPER.writeValueAsString(attribute);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Falha ao serializar variaveis", ex);
    }
  }

  @Override
  public Map<String, String> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isBlank()) {
      return Collections.emptyMap();
    }

    try {
      Map<String, String> parsed = OBJECT_MAPPER.readValue(dbData, MAP_TYPE);
      return new HashMap<>(parsed);
    } catch (JsonProcessingException ex) {
      throw new IllegalArgumentException("Falha ao desserializar variaveis", ex);
    }
  }
}
