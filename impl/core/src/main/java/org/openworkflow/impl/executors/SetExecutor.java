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
package org.openworkflow.impl.executors;

import java.util.concurrent.CompletableFuture;
import org.openworkflow.api.types.Set;
import org.openworkflow.api.types.SetTask;
import org.openworkflow.api.types.SetTaskConfiguration;
import org.openworkflow.impl.TaskContext;
import org.openworkflow.impl.WorkflowContext;
import org.openworkflow.impl.WorkflowDefinition;
import org.openworkflow.impl.WorkflowFilter;
import org.openworkflow.impl.WorkflowModel;
import org.openworkflow.impl.WorkflowMutablePosition;
import org.openworkflow.impl.WorkflowUtils;

public class SetExecutor extends RegularTaskExecutor<SetTask> {

  private final WorkflowFilter setFilter;

  public static class SetExecutorBuilder extends RegularTaskExecutorBuilder<SetTask, SetExecutor> {

    private final WorkflowFilter setFilter;

    protected SetExecutorBuilder(
        WorkflowMutablePosition position, SetTask task, WorkflowDefinition definition) {
      super(position, task, definition);
      Set setInfo = task.getSet();
      SetTaskConfiguration setConfig = setInfo.getSetTaskConfiguration();
      this.setFilter =
          WorkflowUtils.buildWorkflowFilter(
              application,
              setInfo.getString(),
              setConfig != null ? setConfig.getAdditionalProperties() : null);
    }

    @Override
    public SetExecutor buildInstance() {
      return new SetExecutor(this);
    }
  }

  private SetExecutor(SetExecutorBuilder builder) {
    super(builder);
    this.setFilter = builder.setFilter;
  }

  @Override
  protected CompletableFuture<WorkflowModel> internalExecute(
      WorkflowContext workflow, TaskContext taskContext) {
    return CompletableFuture.completedFuture(
        setFilter.apply(workflow, taskContext, taskContext.input()));
  }
}
