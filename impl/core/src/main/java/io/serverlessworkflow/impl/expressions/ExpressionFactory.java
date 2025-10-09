/*
 * Copyright 2020-Present The Serverless Workflow Specification Authors
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
package io.serverlessworkflow.impl.expressions;

import io.cloudevents.CloudEventData;
import io.serverlessworkflow.api.types.TaskBase;
import io.serverlessworkflow.impl.WorkflowFilter;
import io.serverlessworkflow.impl.WorkflowModelFactory;
import io.serverlessworkflow.impl.WorkflowPredicate;
import io.serverlessworkflow.impl.WorkflowValueResolver;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ExpressionFactory {

  static final int DEFAULT_PRIORITY = 1000;
  static final int MIN_PRIORITY = Integer.MAX_VALUE;
  static final int MAX_PRIORITY = Integer.MIN_VALUE;

  default int priority(ExpressionDescriptor desc) {
    return DEFAULT_PRIORITY;
  }

  WorkflowValueResolver<String> resolveString(ExpressionDescriptor desc);

  WorkflowValueResolver<OffsetDateTime> resolveDate(ExpressionDescriptor desc);

  WorkflowValueResolver<CloudEventData> resolveCE(ExpressionDescriptor desc);

  WorkflowValueResolver<Map<String, Object>> resolveMap(ExpressionDescriptor desc);

  WorkflowValueResolver<Collection<?>> resolveCollection(ExpressionDescriptor desc);

  WorkflowFilter buildFilter(ExpressionDescriptor desc, WorkflowModelFactory modelFactory);

  WorkflowPredicate buildPredicate(ExpressionDescriptor desc);

  Optional<WorkflowPredicate> buildIfFilter(TaskBase task);
}
