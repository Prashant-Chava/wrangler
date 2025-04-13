/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.utils;

import io.cdap.wrangler.utils.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {

  @Test
  public void testParseDurations() {
    Assert.assertEquals(5000000L, new TimeDuration("5ms").getValue()); // 5 ms = 5,000,000 ns
    Assert.assertEquals(2100000000L, new TimeDuration("2.1s").getValue()); // 2.1 s = 2.1e9 ns
    Assert.assertEquals(120000000000L, new TimeDuration("2m").getValue()); // 2 min = 120e9 ns
    Assert.assertEquals(3600000000000L, new TimeDuration("1h").getValue()); // 1 hr = 3600e9 ns
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTime() {
    new TimeDuration("2xyz");
  }
}
