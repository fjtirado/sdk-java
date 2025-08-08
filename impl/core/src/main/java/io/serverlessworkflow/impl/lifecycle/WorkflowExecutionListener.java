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
package io.serverlessworkflow.impl.lifecycle;

public interface WorkflowExecutionListener {

  default void onWorkflowStarted(WorkflowStartedEvent ev) {}

  default void onWorkflowSuspended(WorkflowSuspendedEvent ev) {}

  default void onWorkflowResumed(WorkflowResumedEvent ev) {}

  default void onWorkflowCompleted(WorkflowCompletedEvent ev) {}

  default void onWorkflowFailed(WorkflowFailedEvent ev) {}

  default void onWorkflowCancelled(WorkflowCancelledEvent ev) {}

  default void onTaskStarted(TaskStartedEvent ev) {}

  default void onTaskCompleted(TaskCompletedEvent ev) {}

  default void onTaskFailed(TaskFailedEvent ev) {}

  default void onTaskCancelled(TaskCancelledEvent ev) {}

  default void onTaskSuspended(TaskSuspendedEvent ev) {}

  default void onTaskResumed(TaskResumedEvent ev) {}

  default void onTaskRetried(TaskRetriedEvent ev) {}
}
