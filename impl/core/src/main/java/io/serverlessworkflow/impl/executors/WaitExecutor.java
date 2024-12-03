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
package io.serverlessworkflow.impl.executors;

import io.serverlessworkflow.api.types.WaitTask;
import io.serverlessworkflow.impl.TaskContext;
import io.serverlessworkflow.impl.WorkflowContext;
import io.serverlessworkflow.impl.WorkflowDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WaitExecutor extends AbstractTaskExecutor<WaitTask> {

  private static Logger logger = LoggerFactory.getLogger(WaitExecutor.class);
  private final long millisToWait;

  protected WaitExecutor(WaitTask task, WorkflowDefinition definition) {
    super(task, definition);
    // TODO complete this
    this.millisToWait = task.getWait().getDurationInline().getSeconds();
  }

  @Override
  protected void internalExecute(WorkflowContext workflow, TaskContext<WaitTask> taskContext) {
    try {
      Thread.sleep(millisToWait);
    } catch (InterruptedException e) {
      logger.warn("Waiting thread was interrupted", e);
      Thread.currentThread().interrupt();
    }
  }
}
