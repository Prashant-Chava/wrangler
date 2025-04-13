/*
 * Copyright © 2025 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.directives.aggregates;

import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveDefinition;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.annotations.Description;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.Usage;
import io.cdap.wrangler.api.store.ValueStore; // Using ValueStore from wrangler-api
import java.util.List;

@Description("Aggregates byte size and time duration columns.")
@Usage(order = 8, args = {
    @Usage.Argument(name = "byteSizeColumn", type = TokenType.COLUMN_NAME),
    @Usage.Argument(name = "timeDurationColumn", type = TokenType.COLUMN_NAME),
    @Usage.Argument(name = "totalSizeColumn", type = TokenType.IDENTIFIER),
    @Usage.Argument(name = "totalTimeColumn", type = TokenType.IDENTIFIER),
    @Usage.Argument(name = "aggregationType", type = TokenType.IDENTIFIER),
    @Usage.Argument(name = "outputSizeUnit", type = TokenType.IDENTIFIER),
    @Usage.Argument(name = "outputTimeUnit", type = TokenType.IDENTIFIER)
})
@Categories(categories = {"aggregate"})
public class AggregationDirective implements Directive {

  private String byteSizeColumn;
  private String timeDurationColumn;
  private String totalSizeColumn;
  private String totalTimeColumn;
  private String aggregationType;
  private String outputSizeUnit;
  private String outputTimeUnit;

  public AggregationDirective(String byteSizeColumn, String timeDurationColumn,
                              String totalSizeColumn, String totalTimeColumn,
                              String aggregationType, String outputSizeUnit, String outputTimeUnit) {
    this.byteSizeColumn = byteSizeColumn;
    this.timeDurationColumn = timeDurationColumn;
    this.totalSizeColumn = totalSizeColumn;
    this.totalTimeColumn = totalTimeColumn;
    this.aggregationType = aggregationType;
    this.outputSizeUnit = outputSizeUnit;
    this.outputTimeUnit = outputTimeUnit;
  }

  @Override
  public DirectiveDefinition define() {
    return new DirectiveDefinition("aggregateTotals", AggregationDirective.class.getName());
  }

  @Override
  public void initialize(ExecutorContext context) throws Exception {
    // Initialize the store in the context if it doesn't exist
    if (context.getValueStore().get("totalByteSize") == null) {
      context.getValueStore().put("totalByteSize", 0L);
      context.getValueStore().put("totalTimeDuration", 0L);
      context.getValueStore().put("rowCount", 0L);
    }
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws Exception {
    ValueStore store = context.getValueStore();
    for (Row row : rows) {
      Object byteSizeObj = row.getValue(byteSizeColumn);
      Object timeDurationObj = row.getValue(timeDurationColumn);

      if (byteSizeObj != null && timeDurationObj != null) {
        try {
          long byteSize = ((Number) byteSizeObj).longValue();
          long timeDuration = ((Number) timeDurationObj).longValue();

          long totalByteSize = (long) store.get("totalByteSize") + byteSize;
          long totalTimeDuration = (long) store.get("totalTimeDuration") + timeDuration;
          long rowCount = (long) store.get("rowCount") + 1;

          store.put("totalByteSize", totalByteSize);
          store.put("totalTimeDuration", totalTimeDuration);
          store.put("rowCount", rowCount);
        } catch (ClassCastException e) {
          // Handle cases where the column values are not numbers
          context.getLogger().warn("Skipping row due to non-numeric values in aggregation columns: " + row);
        }
      }
    }
    return rows;
  }

  @Override
  public List<Row> finalize(ExecutorContext context) throws Exception {
    ValueStore store = context.getValueStore();
    long totalByteSize = (long) store.get("totalByteSize");
    long totalTimeDuration = (long) store.get("totalTimeDuration");
    long rowCount = (long) store.get("rowCount");

    double finalTotalSize = convertSize(totalByteSize);
    double finalTotalTime = convertTime(totalTimeDuration);

    Row result = new Row();
    result.add(totalSizeColumn, finalTotalSize);
    result.add(totalTimeColumn, finalTotalTime);

    return java.util.Collections.singletonList(result); // finalize should return a single row or a list of final rows
  }

  private double convertSize(long byteSize) {
    String unit = outputSizeUnit.trim().toUpperCase();
    if ("MB".equals(unit)) {
      return byteSize / (1024.0 * 1024.0);
    } else if ("GB".equals(unit)) {
      return byteSize / (1024.0 * 1024.0 * 1024.0);
    } else {
      return (double) byteSize; // Default to bytes
    }
  }

  private double convertTime(long timeDuration) {
    String unit = outputTimeUnit.trim().toLowerCase();
    if ("seconds".equals(unit)) {
      return timeDuration / 1e9;
    } else if ("minutes".equals(unit)) {
      return timeDuration / 60e9;
    } else {
      return (double) timeDuration; // Default to nanoseconds
    }
  }

  @Override
  public void configure(DirectiveDefinition.Arguments args) throws IllegalArgumentException {
    this.byteSizeColumn = ((Identifier) args.get("byteSizeColumn")).value();
    this.timeDurationColumn = ((Identifier) args.get("timeDurationColumn")).value();
    this.totalSizeColumn = ((Identifier) args.get("totalSizeColumn")).value();
    this.totalTimeColumn = ((Identifier) args.get("totalTimeColumn")).value();
    this.aggregationType = ((Identifier) args.get("aggregationType")).value();
    this.outputSizeUnit = ((Identifier) args.get("outputSizeUnit")).value();
    this.outputTimeUnit = ((Identifier) args.get("outputTimeUnit")).value();
  }
}