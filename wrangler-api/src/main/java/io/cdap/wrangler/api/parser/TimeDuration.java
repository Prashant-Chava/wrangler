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
 * Represents a time duration value and provides methods to parse and convert various
 * time units (milliseconds, seconds, minutes, hours, days) into milliseconds.
 */
public class TimeDuration implements Token {
  
  private long valueInMillis;

  /**
   * Constructs a TimeDuration object from a token string representing a time duration.
   *
   * @param token A string representing the time duration (e.g., "10s", "5m").
   */
  public TimeDuration(String token) {
    parse(token);
  }

  /**
   * Parses the token string and converts it to the equivalent time duration in milliseconds.
   *
   * @param token A string representing the time duration (e.g., "10s", "5m").
   */
  private void parse(String token) {
    token = token.toLowerCase().trim();
    String numericValue = token.replaceAll("[^0-9.]", ""); // Extract numeric part
    String unit = token.replaceAll("[0-9.]", ""); // Extract unit part

    // Convert the numeric value to a long
    double value = Double.parseDouble(numericValue);

    // Convert the value to milliseconds based on the unit
    switch (unit) {
      case "ms":
        valueInMillis = (long) value;
        break;
      case "s":
        valueInMillis = (long) (value * 1000);
        break;
      case "m":
        valueInMillis = (long) (value * 1000 * 60);
        break;
      case "h":
        valueInMillis = (long) (value * 1000 * 60 * 60);
        break;
      case "d":
        valueInMillis = (long) (value * 1000 * 60 * 60 * 24);
        break;
      default: // Assume milliseconds if no unit is specified
        valueInMillis = (long) value;
        break;
    }
  }

  /**
   * Returns the value in milliseconds.
   *
   * @return The time duration value in milliseconds.
   */
  public long getMilliseconds() {
    return valueInMillis;
  }

  /**
   * Returns the value of this TimeDuration object.
   *
   * @return The time duration value as an Object (specifically the value in milliseconds).
   */
  @Override
  public Object value() {
    return valueInMillis;
  }

  /**
   * Returns the type of this token as TIME_DURATION.
   *
   * @return The TokenType representing TIME_DURATION.
   */
  @Override
  public TokenType type() {
    return TokenType.TIME_DURATION;
  }

  /**
   * Converts this TimeDuration object to a JSON representation.
   *
   * @return A JsonElement representing the time duration in JSON format.
   */
  @Override
  public JsonElement toJson() {
    return new JsonPrimitive(valueInMillis);
  }
}
