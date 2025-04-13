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

package io.cdap.wrangler;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.executor.ExecutorContext;
import io.cdap.wrangler.api.executor.Store;
import io.cdap.wrangler.directive.AggregateStats;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsDirectiveTest {

  private AggregateStats directive;

  @Before
  public void setup() throws Exception {
    // Initialize the directive with parameters
    directive = new AggregateStats();
    directive.initialize(
        Arrays.asList("size", "time", "total_size", "total_time", "MB", "seconds", "total")
    );
  }

  @Test
  public void testAggregationTotal() throws Exception {
    Row row1 = new Row("size", "1KB").add("time", "1s");
    Row row2 = new Row("size", "2048B").add("time", "2s");
    Row row3 = new Row("size", "0.5MB").add("time", "1.5s");

    Store store = new Store();
    ExecutorContext context = new ExecutorContext() {
      @Override public Store getStore() { return store; }
    };

    directive.execute(context, row1);
    directive.execute(context, row2);
    List<Row> result = directive.execute(context, row3); // Final row returns result

    Assert.assertEquals(1, result.size());

    Row finalRow = result.get(0);

    double expectedSizeInMB = (1024 + 2048 + (0.5 * 1024 * 1024)) / (1024.0 * 1024.0);
    double expectedTimeInSec = 1 + 2 + 1.5;

    double actualSize = (double) finalRow.getValue("total_size");
    double actualTime = (double) finalRow.getValue("total_time");

    Assert.assertEquals(expectedSizeInMB, actualSize, 0.001);
    Assert.assertEquals(expectedTimeInSec, actualTime, 0.001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidSizeInput() throws Exception {
    Row row = new Row("size", "xyz").add("time", "1s");
    directive.execute(new ExecutorContext() {
      @Override public Store getStore() { return new Store(); }
    }, row);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTimeInput() throws Exception {
    Row row = new Row("size", "1kb").add("time", "abc");
    directive.execute(new ExecutorContext() {
      @Override public Store getStore() { return new Store(); }
    }, row);
  }
}
