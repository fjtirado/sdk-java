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
package io.serverlessworkflow.impl.executors.retry;

import io.serverlessworkflow.impl.TaskContext;
import io.serverlessworkflow.impl.WorkflowContext;
import io.serverlessworkflow.impl.WorkflowModel;
import io.serverlessworkflow.impl.WorkflowPredicate;
import io.serverlessworkflow.impl.WorkflowUtils;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DefaultRetryExecutor implements RetryExecutor {

  private final int maxAttempts;
  private final RetryIntervalFunction intervalFunction;
  private final Optional<WorkflowPredicate> whenFilter;
  private final Optional<WorkflowPredicate> exceptFilter;

  public DefaultRetryExecutor(
      int maxAttempts,
      RetryIntervalFunction intervalFunction,
      Optional<WorkflowPredicate> whenFilter,
      Optional<WorkflowPredicate> exceptFilter) {
    this.maxAttempts = maxAttempts;
    this.intervalFunction = intervalFunction;
    this.whenFilter = whenFilter;
    this.exceptFilter = exceptFilter;
  }

  @Override
  public CompletableFuture<WorkflowModel> retry(
      WorkflowContext workflowContext, TaskContext taskContext, WorkflowModel model) {
    CompletableFuture<WorkflowModel> completable = new CompletableFuture<>();
    short numAttempts = taskContext.retryAttempt();
    if (numAttempts++ < maxAttempts
        && WorkflowUtils.whenExceptTest(
            whenFilter, exceptFilter, workflowContext, taskContext, model)) {
      taskContext.retryAttempt(numAttempts);
      Duration delay = intervalFunction.apply(workflowContext, taskContext, model, numAttempts);
      completable.completeOnTimeout(model, delay.toMillis(), TimeUnit.MILLISECONDS);
    } else {
      completable.complete(model);
    }
    return completable;
  }
}
