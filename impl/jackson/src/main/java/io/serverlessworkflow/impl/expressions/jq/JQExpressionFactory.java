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
package io.serverlessworkflow.impl.expressions.jq;

import io.cloudevents.CloudEventData;
import io.cloudevents.jackson.JsonCloudEventData;
import io.serverlessworkflow.impl.WorkflowModelFactory;
import io.serverlessworkflow.impl.expressions.ExpressionUtils;
import io.serverlessworkflow.impl.expressions.ObjectExpression;
import io.serverlessworkflow.impl.expressions.ObjectExpressionFactory;
import io.serverlessworkflow.impl.jackson.JsonUtils;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import net.thisptr.jackson.jq.BuiltinFunctionLoader;
import net.thisptr.jackson.jq.Scope;
import net.thisptr.jackson.jq.Versions;
import net.thisptr.jackson.jq.exception.JsonQueryException;

public class JQExpressionFactory extends ObjectExpressionFactory {

  private WorkflowModelFactory modelFactory = new JacksonModelFactory();

  private static Supplier<Scope> scopeSupplier = new DefaultScopeSupplier();

  private static class DefaultScopeSupplier implements Supplier<Scope> {
    private static class DefaultScope {
      private static Scope scope;

      static {
        scope = Scope.newEmptyScope();
        BuiltinFunctionLoader.getInstance().loadFunctions(Versions.JQ_1_6, scope);
      }
    }

    @Override
    public Scope get() {
      return DefaultScope.scope;
    }
  }

  @Override
  public ObjectExpression buildExpression(String expression) {
    try {
      return new JQExpression(scopeSupplier, ExpressionUtils.trimExpr(expression), Versions.JQ_1_6);
    } catch (JsonQueryException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public WorkflowModelFactory modelFactory() {
    return modelFactory;
  }

  @Override
  protected boolean toBoolean(Object eval) {
    return JsonUtils.convertValue(eval, Boolean.class);
  }

  @Override
  protected String toString(Object eval) {
    return JsonUtils.convertValue(eval, String.class);
  }

  @Override
  protected CloudEventData toCloudEventData(Object eval) {
    return JsonCloudEventData.wrap(JsonUtils.fromValue(eval));
  }

  @Override
  protected OffsetDateTime toDate(Object eval) {
    return JsonUtils.toDate(JsonUtils.fromValue(eval))
        .orElseThrow(
            () -> new IllegalStateException("Cannot convert " + eval + " to OffseDateTime"));
  }

  @Override
  protected Map<String, Object> toMap(Object eval) {
    return (Map<String, Object>) JsonUtils.toJavaValue(eval);
  }

  @Override
  protected Object toJavaObject(Object eval) {
    return JsonUtils.toJavaValue(eval);
  }

  @Override
  protected Collection<?> toCollection(Object obj) {
    return (Collection<?>) JsonUtils.toJavaValue(obj);
  }
}
