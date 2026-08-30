package de.sw1ld;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = false)
public class PositionListBinaryConverter implements AttributeConverter<List<Position>, byte[]> {

  private static final int BYTES_PER_POS = 4 + 4 + 4; // lat(float), lon(float), altitude(float)

  @Override
  public byte[] convertToDatabaseColumn(List<Position> positions) {
    if (positions == null || positions.isEmpty()) return new byte[0];
    ByteBuffer buffer = ByteBuffer.allocate(positions.size() * BYTES_PER_POS);
    for (Position p : positions) {
      buffer.putFloat(p.lat());
      buffer.putFloat(p.lon());
      buffer.putFloat(p.altitude());
    }
    return buffer.array();
  }

  @Override
  public List<Position> convertToEntityAttribute(byte[] dbData) {
    if (dbData == null || dbData.length == 0) return List.of();
    ByteBuffer buffer = ByteBuffer.wrap(dbData);
    int count = dbData.length / BYTES_PER_POS;
    List<Position> result = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      result.add(new Position(buffer.getFloat(), buffer.getFloat(), buffer.getFloat()));
    }
    return result;
  }
}
