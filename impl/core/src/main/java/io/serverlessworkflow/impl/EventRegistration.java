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
package io.serverlessworkflow.impl;

import io.cloudevents.CloudEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record EventRegistration(
    Consumer<CloudEvent> consumer, String type, Optional<Predicate<CloudEvent>> filter) {

  public static EventRegistrationBuilder builder(String type) {
    return new EventRegistrationBuilder(type);
  }

  public static class EventRegistrationBuilder {

    private final String type;
    private Consumer<CloudEvent> consumer;
    private Predicate<CloudEvent> predicate;

    private EventRegistrationBuilder(String type) {
      this.type = type;
    }

    public EventRegistrationBuilder withConsumer(Consumer<CloudEvent> consumer) {
      this.consumer = consumer;
      return this;
    }

    public EventRegistrationBuilder withFilter(Predicate<CloudEvent> predicate) {
      this.predicate = predicate;
      return this;
    }

    public EventRegistration build() {
      return new EventRegistration(
          Objects.requireNonNull(consumer), type, Optional.ofNullable(predicate));
    }
  }
}
