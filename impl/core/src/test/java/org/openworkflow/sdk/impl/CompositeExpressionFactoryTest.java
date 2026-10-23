/*
 * Copyright 2020-Present The Open Workflow Specification Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openworkflow.sdk.impl;

import static org.assertj.core.api.Assertions.assertThat;

import io.cloudevents.CloudEventData;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.openworkflow.sdk.api.types.TaskBase;
import org.openworkflow.sdk.impl.expressions.ExpressionDescriptor;
import org.openworkflow.sdk.impl.expressions.ExpressionFactory;

class CompositeExpressionFactoryTest {

  private static final ExpressionDescriptor DESC = ExpressionDescriptor.from(".foo");

  @Test
  void factoryDeclaringMaxPriorityWinsOverDefaultPriority() {
    WorkflowPredicate expected = (w, t, m) -> true;
    WorkflowPredicate unexpected = (w, t, m) -> false;
    ExpressionFactory defaultPriority =
        new ExprFactory(ExpressionFactory.DEFAULT_PRIORITY, unexpected);
    ExpressionFactory shouldAlwaysWin = new ExprFactory(ExpressionFactory.MAX_PRIORITY, expected);

    ExpressionFactory composite =
        new CompositeExpressionFactory(List.of(defaultPriority, shouldAlwaysWin));

    // ExpressionFactory.MAX_PRIORITY is documented as "I must win": a factory declaring it
    // must be picked over one using the plain DEFAULT_PRIORITY.
    assertThat(composite.buildPredicate(DESC)).isSameAs(expected);
  }

  @Test
  void factoryDeclaringMinPriorityLosesToDefaultPriority() {
    WorkflowPredicate expected = (w, t, m) -> true;
    WorkflowPredicate unexpected = (w, t, m) -> false;
    ExpressionFactory defaultPriority =
        new ExprFactory(ExpressionFactory.DEFAULT_PRIORITY, expected);
    ExpressionFactory shouldAlwaysLose =
        new ExprFactory(ExpressionFactory.MIN_PRIORITY, unexpected);

    ExpressionFactory composite =
        new CompositeExpressionFactory(List.of(defaultPriority, shouldAlwaysLose));

    assertThat(composite.buildPredicate(DESC)).isSameAs(expected);
  }

  @Test
  void factoryDeclaringMaxPriorityWinsOverFactoryDeclaringMinPriority() {
    WorkflowPredicate expected = (w, t, m) -> true;
    WorkflowPredicate unexpected = (w, t, m) -> false;
    ExpressionFactory shouldAlwaysLose =
        new ExprFactory(ExpressionFactory.MIN_PRIORITY, unexpected);
    ExpressionFactory shouldAlwaysWin = new ExprFactory(ExpressionFactory.MAX_PRIORITY, expected);

    ExpressionFactory composite =
        new CompositeExpressionFactory(List.of(shouldAlwaysLose, shouldAlwaysWin));

    assertThat(composite.buildPredicate(DESC)).isSameAs(expected);
  }

  @Test
  void factoryDeclaringMaxPriorityWinsAmongSeveralCompetingPriorities() {
    WorkflowPredicate expected = (w, t, m) -> true;
    ExpressionFactory shouldAlwaysWin = new ExprFactory(ExpressionFactory.MAX_PRIORITY, expected);

    ExpressionFactory composite =
        new CompositeExpressionFactory(
            List.of(
                new ExprFactory(ExpressionFactory.MIN_PRIORITY, (w, t, m) -> false),
                new ExprFactory(ExpressionFactory.DEFAULT_PRIORITY, (w, t, m) -> false),
                new ExprFactory(-1, (w, t, m) -> false),
                shouldAlwaysWin,
                new ExprFactory(500, (w, t, m) -> false)));

    assertThat(composite.buildPredicate(DESC)).isSameAs(expected);
  }

  private record ExprFactory(int priority, WorkflowPredicate predicate)
      implements ExpressionFactory {

    @Override
    public int priority(ExpressionDescriptor desc) {
      return priority;
    }

    @Override
    public WorkflowPredicate buildPredicate(ExpressionDescriptor desc) {
      return predicate;
    }

    @Override
    public WorkflowValueResolver<String> resolveString(ExpressionDescriptor desc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowValueResolver<Object> resolveValue(ExpressionDescriptor desc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowValueResolver<OffsetDateTime> resolveDate(ExpressionDescriptor desc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowValueResolver<CloudEventData> resolveCE(ExpressionDescriptor desc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowValueResolver<Map<String, Object>> resolveMap(ExpressionDescriptor desc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowValueResolver<Collection<?>> resolveCollection(ExpressionDescriptor desc) {
      throw new UnsupportedOperationException();
    }

    @Override
    public WorkflowFilter buildFilter(
        ExpressionDescriptor desc, WorkflowModelFactory modelFactory) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<WorkflowPredicate> buildIfFilter(TaskBase task) {
      throw new UnsupportedOperationException();
    }
  }
}
