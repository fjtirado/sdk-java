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
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.jackson.JsonCloudEventData;
import io.serverlessworkflow.api.types.EventConsumptionStrategy__4;
import io.serverlessworkflow.api.types.EventFilter;
import io.serverlessworkflow.api.types.ListenTask;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.impl.EventRegistration;
import io.serverlessworkflow.impl.EventRegistrationBuilder;
import io.serverlessworkflow.impl.TaskContext;
import io.serverlessworkflow.impl.WorkflowApplication;
import io.serverlessworkflow.impl.WorkflowContext;
import io.serverlessworkflow.impl.WorkflowFilter;
import io.serverlessworkflow.impl.WorkflowPosition;
import io.serverlessworkflow.impl.json.JsonUtils;
import io.serverlessworkflow.impl.resources.ResourceLoader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class ListenExecutor extends RegularTaskExecutor<ListenTask> {

  protected final Collection<EventRegistrationBuilder<?>> regBuilders;
  protected final Optional<WorkflowFilter> until;
  protected final Optional<TaskExecutor<?>> forEach;
  protected final Function<CloudEvent, JsonNode> converter;

  public static class ListenExecutorBuilder extends RegularTaskExecutorBuilder<ListenTask> {

    private Collection<EventRegistrationBuilder<?>> registrations;
    private WorkflowFilter until;
    private TaskExecutor<?> forEach;
    private Function<CloudEvent, JsonNode> converter;
    private boolean isAnd;

    protected ListenExecutorBuilder(
        WorkflowPosition position,
        ListenTask task,
        Workflow workflow,
        WorkflowApplication application,
        ResourceLoader resourceLoader) {
      super(position, task, workflow, application, resourceLoader);
      if (task.getListen().getTo().getAllEventConsumptionStrategy() != null) {
    	  isAnd = true;
    	  registrations = task.getListen().getTo().getAllEventConsumptionStrategy().getAll().stream().map(this::from).collect(Collectors.toList());
      }
    }
      
    private EventRegistrationBuilder<?> from (EventFilter filter) {
    	return application.eventConsumer().register(filter);
    }
    

    @Override
    public TaskExecutor<ListenTask> buildInstance() {
      return isAnd ? new AndListenExecutor(this) : new OrListenExecutor(this);
    }
  }

  public static class AndListenExecutor extends ListenExecutor {

    public AndListenExecutor(ListenExecutorBuilder builder) {
      super(builder);
    }

    protected CompletableFuture<JsonNode> toCompletable(
        WorkflowContext workflow,
        TaskContext taskContext,
        EventRegistrationBuilder<?> regBuilder,
        Collection<EventRegistration> registrations,
        ArrayNode arrayNode) {
      final CompletableFuture<JsonNode> future = new CompletableFuture<>();
      registrations.add(
          regBuilder.apply(
              ce -> {
                arrayNode.add(converter.apply(ce));
                if (until.isEmpty()
                    || until
                        .filter(u -> u.apply(workflow, taskContext, arrayNode).asBoolean())
                        .isPresent()) {
                  future.complete(arrayNode);
                }
              }));
      return future;
    }

    protected void internalProcessCe(
        JsonNode node,
        ArrayNode arrayNode,
        WorkflowContext workflow,
        TaskContext taskContext,
        CompletableFuture<JsonNode> future) {
      arrayNode.add(node);
      future.complete(node);
    }

    @Override
    protected CompletableFuture<?> combine(CompletableFuture<JsonNode>[] completables) {
      return CompletableFuture.allOf(completables);
    }
  }

  public static class OrListenExecutor extends ListenExecutor {

    public OrListenExecutor(ListenExecutorBuilder builder) {
      super(builder);
    }

    @Override
    protected CompletableFuture<?> combine(CompletableFuture<JsonNode>[] completables) {
      return CompletableFuture.anyOf(completables);
    }

    protected void internalProcessCe(
        JsonNode node,
        ArrayNode arrayNode,
        WorkflowContext workflow,
        TaskContext taskContext,
        CompletableFuture<JsonNode> future) {
      if (until.isEmpty()
          || until.filter(u -> u.apply(workflow, taskContext, arrayNode).asBoolean()).isPresent()) {
        future.complete(arrayNode);
      }
    }
  }

  protected abstract CompletableFuture<?> combine(CompletableFuture<JsonNode>[] completables);

  protected abstract void internalProcessCe(
      JsonNode node,
      ArrayNode arrayNode,
      WorkflowContext workflow,
      TaskContext taskContext,
      CompletableFuture<JsonNode> future);
  
   private void processCe(
	      JsonNode node,
	      ArrayNode arrayNode,
	      WorkflowContext workflow,
	      TaskContext taskContext,
	      CompletableFuture<JsonNode> future) {
	   forEach.ifPresentOrElse(t ->  
             TaskExecutorHelper.processTaskList(
                      t, workflow, Optional.of(taskContext), node).thenAccept(n -> internalProcessCe(n, arrayNode, workflow, taskContext, future)), () -> internalProcessCe(node, arrayNode, workflow, taskContext, future));
   }
  

  protected CompletableFuture<JsonNode> toCompletable(
      WorkflowContext workflow,
      TaskContext taskContext,
      EventRegistrationBuilder<?> regBuilder,
      Collection<EventRegistration> registrations,
      ArrayNode arrayNode) {
    final CompletableFuture<JsonNode> future = new CompletableFuture<>();
    registrations.add(
        regBuilder.apply(
            ce -> processCe(converter.apply(ce), arrayNode, workflow, taskContext, future)));
    return future;
  }

  @Override
  protected CompletableFuture<JsonNode> internalExecute(
      WorkflowContext workflow, TaskContext taskContext) {
    Collection<EventRegistration> registrations = new ArrayList<>();
    ArrayNode output = JsonUtils.mapper().createArrayNode();
    return combine(
            regBuilders.stream()
                .map(reg -> toCompletable(workflow, taskContext, reg, registrations, output))
                .toArray(size -> new CompletableFuture[size]))
        .thenApply(
            v -> {
              registrations.forEach(
                  reg -> workflow.definition().application().eventConsumer().unregister(reg));
              return output;
            });
  }

  protected ListenExecutor(ListenExecutorBuilder builder) {
    super(builder);
    this.regBuilders = builder.registrations;
    this.until = Optional.ofNullable(builder.until);
    this.forEach = Optional.ofNullable(builder.forEach);
    this.converter = builder.converter;
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
