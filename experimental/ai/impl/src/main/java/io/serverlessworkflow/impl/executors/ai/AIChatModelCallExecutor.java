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

package io.serverlessworkflow.impl.executors.ai;

import io.serverlessworkflow.ai.api.types.CallAgentAI;
import io.serverlessworkflow.api.types.TaskBase;
import io.serverlessworkflow.impl.TaskContext;
import io.serverlessworkflow.impl.WorkflowApplication;
import io.serverlessworkflow.impl.WorkflowContext;
import io.serverlessworkflow.impl.WorkflowModel;
import io.serverlessworkflow.impl.WorkflowModelFactory;
import io.serverlessworkflow.impl.executors.CallableTask;
import io.serverlessworkflow.impl.resources.ResourceLoader;
import java.util.concurrent.CompletableFuture;

public class AIChatModelCallExecutor implements CallableTask<CallAgentAI> {

  @Override
  public void init(CallAgentAI task, WorkflowApplication application, ResourceLoader loader) {}

  @Override
  public CompletableFuture<WorkflowModel> apply(
      WorkflowContext workflowContext, TaskContext taskContext, WorkflowModel input) {
    WorkflowModelFactory modelFactory = workflowContext.definition().application().modelFactory();
    if (taskContext.task() instanceof CallAgentAI agenticAI) {
      return CompletableFuture.completedFuture(
          modelFactory.fromAny(new CallAgentAIExecutor().execute(agenticAI, input.asJavaObject())));
    }
    throw new IllegalArgumentException(
        "AIChatModelCallExecutor can only process CallAgentAI tasks, but received: "
            + taskContext.task().getClass().getName());
  }

  @Override
  public boolean accept(Class<? extends TaskBase> clazz) {
    return CallAgentAI.class.isAssignableFrom(clazz);
  }
}
