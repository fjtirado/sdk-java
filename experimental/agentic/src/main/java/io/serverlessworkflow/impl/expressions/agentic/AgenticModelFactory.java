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
package io.serverlessworkflow.impl.expressions.agentic;

import dev.langchain4j.agentic.cognisphere.Cognisphere;
import dev.langchain4j.agentic.cognisphere.CognisphereRegistry;
import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.serverlessworkflow.impl.WorkflowModel;
import io.serverlessworkflow.impl.WorkflowModelCollection;
import io.serverlessworkflow.impl.WorkflowModelFactory;
import io.serverlessworkflow.impl.expressions.func.JavaModel;

import java.time.OffsetDateTime;
import java.util.Map;

class AgenticModelFactory implements WorkflowModelFactory {

  
  @Override
  public WorkflowModel fromAny(WorkflowModel prev, Object obj) {
    ((AgenticModel) prev).setObject(obj);
    return prev;
  }

  @Override
  public WorkflowModel combine(Map<String, WorkflowModel> workflowVariables) {
	 //todo combine cognishphere
    return new JavaModel(workflowVariables);
  }

  @Override
  public WorkflowModelCollection createCollection() {
   throw new UnsupportedOperationException();
  }

  @Override
  public WorkflowModel from(boolean value) {
    return new JavaModel(value);
  }

  @Override
  public WorkflowModel from(Number value) {
    return new JavaModel(value);
  }

  @Override
  public WorkflowModel from(String value) {
    return new JavaModel(value);
  }

  @Override
  public WorkflowModel from(CloudEvent ce) {
    return new JavaModel(ce);
  }

  @Override
  public WorkflowModel from(CloudEventData ce) {
    return new JavaModel(ce);
  }

  @Override
  public WorkflowModel from(OffsetDateTime value) {
    return new JavaModel(value);
  }

  @Override
  public WorkflowModel from(Map<String, Object> map) {
    return new JavaModel(map);
  }

  @Override
  public WorkflowModel fromNull() {
    return new JavaModel(null);
  }

  @Override
  public WorkflowModel fromOther(Object value) {
    return new AgenticModel((Cognisphere) value);
  }
}
