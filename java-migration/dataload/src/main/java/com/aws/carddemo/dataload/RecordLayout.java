package com.aws.carddemo.dataload;

import com.aws.carddemo.common.CobolDecimal;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record RecordLayout(int recordLength, List<FieldDefinition> fields) {
  public RecordLayout {
    fields = List.copyOf(fields);
  }

  public Map<String, Object> parse(byte[] record, Charset charset) {
    if (record.length < recordLength) {
      throw new IllegalArgumentException(
          "Record has " + record.length + " bytes; expected " + recordLength);
    }
    String text = new String(record, charset);
    Map<String, Object> values = new LinkedHashMap<>();
    for (FieldDefinition field : fields) {
      values.put(field.name(), field.read(record, text));
    }
    return values;
  }

  public enum Type {
    TEXT,
    INTEGER,
    LONG,
    ZONED_DECIMAL,
    COMP3
  }

  public record FieldDefinition(String name, int offset, int length, Type type, int scale) {
    Object read(byte[] record, String text) {
      if (type == Type.COMP3) {
        byte[] value = new byte[length];
        System.arraycopy(record, offset, value, 0, length);
        return CobolDecimal.decodeComp3(value, scale);
      }
      String value = text.substring(offset, offset + length);
      if (type == Type.TEXT) {
        return value.replace('\0', ' ').stripTrailing();
      }
      if (value
          .codePoints()
          .allMatch(character -> character == 0 || Character.isWhitespace(character))) {
        return null;
      }
      return switch (type) {
        case INTEGER -> Integer.valueOf(value.trim());
        case LONG -> Long.valueOf(value.trim());
        case ZONED_DECIMAL -> CobolDecimal.decodeZoned(value, scale);
        default -> throw new IllegalStateException("Unsupported field type " + type);
      };
    }
  }
}
