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
import dev.langchain4j.agentic.cognisphere.ResultWithCognisphere;
import io.serverlessworkflow.impl.WorkflowModel;
import io.serverlessworkflow.impl.expressions.func.JavaModel;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

class AgenticModel implements WorkflowModel {

  private final Cognisphere cognisphere;
  private Object output;

  AgenticModel(Cognisphere cognisphere) {
    this.cognisphere = cognisphere;
  }

  public void setObject(Object obj) {
    this.output = obj;
  }

  @Override
  public Collection<WorkflowModel> asCollection() {
	  throw new UnsupportedOperationException();
  }

  @Override
  public <T> Optional<T> as(Class<T> clazz) {
    if (Cognisphere.class.isAssignableFrom(clazz)) {
      return Optional.of(clazz.cast(cognisphere));
    } else if (ResultWithCognisphere.class.isAssignableFrom(clazz)) {
      return Optional.of(clazz.cast(new ResultWithCognisphere<>(cognisphere, output)));
    } else {
      return Optional.of(clazz.cast(output));
    }
  }

  @Override
  public void forEach(BiConsumer<String, WorkflowModel> consumer) {
	  throw new UnsupportedOperationException();
  }

  @Override
  public Optional<Boolean> asBoolean() {
	return Optional.empty();
  }

  @Override
  public Optional<String> asText() {
	return Optional.empty();
  }

  @Override
  public Optional<OffsetDateTime> asDate() {
	return Optional.empty();
  }

  @Override
  public Optional<Number> asNumber() {
	return Optional.empty();
  }

  @Override
  public Optional<Map<String, Object>> asMap() {
	return Optional.empty();
  }

  @Override
  public Object asJavaObject() {
	return output;
  }

  @Override
  public Object asIs() {
	return cognisphere;
  }

  @Override
  public Class<?> objectClass() {
	return Cognisphere.class;
  }
}
