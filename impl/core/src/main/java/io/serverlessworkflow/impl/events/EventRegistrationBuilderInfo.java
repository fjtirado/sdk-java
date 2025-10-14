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
package io.serverlessworkflow.impl.events;

import io.serverlessworkflow.api.types.AllEventConsumptionStrategy;
import io.serverlessworkflow.api.types.AnyEventConsumptionStrategy;
import io.serverlessworkflow.api.types.EventConsumptionStrategy;
import io.serverlessworkflow.api.types.EventFilter;
import io.serverlessworkflow.api.types.ListenTo;
import io.serverlessworkflow.api.types.OneEventConsumptionStrategy;
import io.serverlessworkflow.api.types.Until;
import io.serverlessworkflow.impl.WorkflowApplication;
import io.serverlessworkflow.impl.WorkflowPredicate;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record EventRegistrationBuilderInfo(
    EventRegistrationBuilderCollection registrations,
    EventRegistrationBuilderCollection untilRegistrations,
    WorkflowPredicate until) {

  public static EventRegistrationBuilderInfo from(
      WorkflowApplication application,
      ListenTo to,
      Function<Until, WorkflowPredicate> predBuilder) {
    EventRegistrationBuilderCollection registrations;
    EventRegistrationBuilderCollection untilRegistrations = null;
    WorkflowPredicate until = null;
    if (to.getAllEventConsumptionStrategy() != null) {
      registrations = allEvents(to.getAllEventConsumptionStrategy(), application);
    } else if (to.getAnyEventConsumptionStrategy() != null) {
      AnyEventConsumptionStrategy any = to.getAnyEventConsumptionStrategy();
      registrations = anyEvents(any, application);
      Until untilDesc = any.getUntil();
      if (untilDesc != null) {
        until = predBuilder.apply(untilDesc);
        if (until == null) {
          if (untilDesc.getAnyEventUntilConsumed() != null) {
            EventConsumptionStrategy strategy = untilDesc.getAnyEventUntilConsumed();
            if (strategy.getAllEventConsumptionStrategy() != null) {
              untilRegistrations =
                  allEvents(strategy.getAllEventConsumptionStrategy(), application);
            } else if (strategy.getAnyEventConsumptionStrategy() != null) {
              untilRegistrations =
                  anyEvents(strategy.getAnyEventConsumptionStrategy(), application);
            } else if (strategy.getOneEventConsumptionStrategy() != null) {
              untilRegistrations = oneEvent(strategy.getOneEventConsumptionStrategy(), application);
            }
          }
        }
      }
    } else {
      registrations = oneEvent(to.getOneEventConsumptionStrategy(), application);
    }
    return new EventRegistrationBuilderInfo(registrations, untilRegistrations, until);
  }

  private static EventRegistrationBuilderCollection allEvents(
      AllEventConsumptionStrategy allStrategy, WorkflowApplication application) {
    return new EventRegistrationBuilderCollection(from(allStrategy.getAll(), application), true);
  }

  private static EventRegistrationBuilderCollection anyEvents(
      AnyEventConsumptionStrategy anyStrategy, WorkflowApplication application) {
    List<EventFilter> eventFilters = anyStrategy.getAny();
    return new EventRegistrationBuilderCollection(
        eventFilters.isEmpty() ? registerToAll(application) : from(eventFilters, application),
        false);
  }

  private static EventRegistrationBuilderCollection oneEvent(
      OneEventConsumptionStrategy oneStrategy, WorkflowApplication application) {
    return new EventRegistrationBuilderCollection(
        List.of(from(oneStrategy.getOne(), application)), true);
  }

  private static Collection<EventRegistrationBuilder> registerToAll(
      WorkflowApplication application) {
    return application.eventConsumer().listenToAll(application);
  }

  private static Collection<EventRegistrationBuilder> from(
      List<EventFilter> filters, WorkflowApplication application) {
    return filters.stream().map(filter -> from(filter, application)).collect(Collectors.toList());
  }

  private static EventRegistrationBuilder from(
      EventFilter filter, WorkflowApplication application) {
    return application.eventConsumer().listen(filter, application);
  }
}
