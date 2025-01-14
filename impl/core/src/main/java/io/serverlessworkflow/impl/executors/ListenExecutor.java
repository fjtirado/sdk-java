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

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.CloudEventData;
import io.cloudevents.jackson.JsonCloudEventData;
import io.serverlessworkflow.api.types.ListenTask;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.impl.EventRegistration.EventRegistrationBuilder;
import io.serverlessworkflow.impl.TaskContext;
import io.serverlessworkflow.impl.WorkflowApplication;
import io.serverlessworkflow.impl.WorkflowContext;
import io.serverlessworkflow.impl.WorkflowPosition;
import io.serverlessworkflow.impl.json.JsonUtils;
import io.serverlessworkflow.impl.resources.ResourceLoader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.CompletableFuture;

public class ListenExecutor extends RegularTaskExecutor<ListenTask> {

  private final EventRegistrationBuilder registration;

  public static class ListenExecutorBuilder extends RegularTaskExecutorBuilder<ListenTask> {

    private EventRegistrationBuilder registration;

    protected ListenExecutorBuilder(
        WorkflowPosition position,
        ListenTask task,
        Workflow workflow,
        WorkflowApplication application,
        ResourceLoader resourceLoader) {
      super(position, task, workflow, application, resourceLoader);
    }

    @Override
    public TaskExecutor<ListenTask> buildInstance() {
      return new ListenExecutor(this);
    }
  }

  private ListenExecutor(ListenExecutorBuilder builder) {
    super(builder);
    this.registration = builder.registration;
  }

  @Override
  protected CompletableFuture<JsonNode> internalExecute(
      WorkflowContext workflow, TaskContext taskContext) {
    CompletableFuture<JsonNode> future = new CompletableFuture<>();
    registration.withConsumer(ce -> future.complete(toJsonNode(ce.getData())));
    return future;
  }

  private JsonNode toJsonNode(CloudEventData data) {
    try {
      return data instanceof JsonCloudEventData
          ? ((JsonCloudEventData) data).getNode()
          : JsonUtils.mapper().readTree(data.toBytes());
    } catch (IOException io) {
      throw new UncheckedIOException(io);
    }
  }
}
