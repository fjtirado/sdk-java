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
package io.serverlessworkflow.api.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.serverlessworkflow.api.events.EventDefinition;
import io.serverlessworkflow.api.interfaces.WorkflowPropertySource;
import io.serverlessworkflow.api.utils.Utils;
import io.serverlessworkflow.api.workflow.Events;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventsDeserializer extends StdDeserializer<Events> {

  private static final long serialVersionUID = 510l;
  private static Logger logger = LoggerFactory.getLogger(EventsDeserializer.class);

  @SuppressWarnings("unused")
  private WorkflowPropertySource context;

  public EventsDeserializer() {
    this(Events.class);
  }

  public EventsDeserializer(Class<?> vc) {
    super(vc);
  }

  public EventsDeserializer(WorkflowPropertySource context) {
    this(Events.class);
    this.context = context;
  }

  @Override
  public Events deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    JsonNode node = jp.getCodec().readTree(jp);

    Events events = new Events();
    List<EventDefinition> eventDefs = new ArrayList<>();

    if (node.isArray()) {
      for (final JsonNode nodeEle : node) {
        eventDefs.add(mapper.treeToValue(nodeEle, EventDefinition.class));
      }
    } else {
      String eventsFileDef = node.asText();
      String eventsFileSrc = Utils.getResourceFileAsString(eventsFileDef);
      if (eventsFileSrc != null && eventsFileSrc.trim().length() > 0) {
        // if its a yaml def convert to json first
        JsonNode eventsRefNode = Utils.getNode(eventsFileSrc);
        JsonNode refEvents = eventsRefNode.get("events");
        if (refEvents != null) {
          for (final JsonNode nodeEle : refEvents) {
            eventDefs.add(mapper.treeToValue(nodeEle, EventDefinition.class));
          }
        } else {
          logger.error("Unable to find event definitions in reference file: {}", eventsFileSrc);
        }

      } else {
        logger.error("Unable to load event defs reference file: {}", eventsFileSrc);
      }
    }
    events.setEventDefs(eventDefs);
    return events;
  }
}
