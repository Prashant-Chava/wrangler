/*
 * Copyright 2025 <Your Organization>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

/**
 * Represents a byte size value and provides methods to parse and convert various
 * byte size units (KB, MB, GB, TB) into bytes.
 */
public class ByteSize implements Token {
  
  private long valueInBytes;

  /**
   * Constructs a ByteSize object from a token string representing a byte size.
   *
   * @param token A string representing the byte size (e.g., "10KB", "5MB").
   */
  public ByteSize(String token) {
    parse(token);
  }

  /**
   * Parses the token string and converts it to the equivalent byte value.
   *
   * @param token A string representing the byte size (e.g., "10KB", "5MB").
   */
  private void parse(String token) {
    token = token.toUpperCase().trim();
    String numericValue = token.replaceAll("[^0-9.]", ""); // Extract numeric part
    String unit = token.replaceAll("[0-9.]", "").toUpperCase(); // Extract unit part

    // Convert the numeric value to a long
    double value = Double.parseDouble(numericValue);

    // Convert the value to bytes based on the unit
    switch (unit) {
      case "KB":
        valueInBytes = (long) (value * 1024);
        break;
      case "MB":
        valueInBytes = (long) (value * 1024 * 1024);
        break;
      case "GB":
        valueInBytes = (long) (value * 1024 * 1024 * 1024);
        break;
      case "TB":
        valueInBytes = (long) (value * 1024 * 1024 * 1024 * 1024);
        break;
      default: // Assume bytes if no unit is specified
        valueInBytes = (long) value;
        break;
    }
  }

  /**
   * Returns the value in bytes.
   *
   * @return The byte size value in bytes.
   */
  public long getBytes() {
    return valueInBytes;
  }

  /**
   * Returns the value of this ByteSize object.
   *
   * @return The byte size value as an Object (specifically the byte value in bytes).
   */
  @Override
  public Object value() {
    return valueInBytes;
  }

  /**
   * Returns the type of this token as BYTE_SIZE.
   *
   * @return The TokenType representing BYTE_SIZE.
   */
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }

  /**
   * Converts this ByteSize object to a JSON representation.
   *
   * @return A JsonElement representing the byte size in JSON format.
   */
  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(valueInBytes);
  }
}
