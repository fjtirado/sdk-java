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
import io.serverlessworkflow.api.interfaces.WorkflowPropertySource;
import io.serverlessworkflow.api.retry.RetryDefinition;
import io.serverlessworkflow.api.utils.Utils;
import io.serverlessworkflow.api.workflow.Retries;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetriesDeserializer extends StdDeserializer<Retries> {

  private static final long serialVersionUID = 510l;
  private static Logger logger = LoggerFactory.getLogger(RetriesDeserializer.class);

  @SuppressWarnings("unused")
  private WorkflowPropertySource context;

  public RetriesDeserializer() {
    this(Retries.class);
  }

  public RetriesDeserializer(Class<?> vc) {
    super(vc);
  }

  public RetriesDeserializer(WorkflowPropertySource context) {
    this(Retries.class);
    this.context = context;
  }

  @Override
  public Retries deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {

    ObjectMapper mapper = (ObjectMapper) jp.getCodec();
    JsonNode node = jp.getCodec().readTree(jp);

    Retries retries = new Retries();
    List<RetryDefinition> retryDefinitions = new ArrayList<>();

    if (node.isArray()) {
      for (final JsonNode nodeEle : node) {
        retryDefinitions.add(mapper.treeToValue(nodeEle, RetryDefinition.class));
      }
    } else {
      String retriesFileDef = node.asText();
      String retriesFileSrc = Utils.getResourceFileAsString(retriesFileDef);
      ;
      if (retriesFileSrc != null && retriesFileSrc.trim().length() > 0) {
        // if its a yaml def convert to json first
        JsonNode retriesRefNode = Utils.getNode(retriesFileSrc);
        JsonNode refRetries = retriesRefNode.get("retries");
        if (refRetries != null) {
          for (final JsonNode nodeEle : refRetries) {
            retryDefinitions.add(mapper.treeToValue(nodeEle, RetryDefinition.class));
          }
        } else {
          logger.error("Unable to find retries definitions in reference file: {}", retriesFileSrc);
        }

      } else {
        logger.error("Unable to load retries defs reference file: {}", retriesFileSrc);
      }
    }
    retries.setRetryDefs(retryDefinitions);
    return retries;
  }
}
