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
package io.serverlessworkflow.impl.test;

import static io.serverlessworkflow.api.WorkflowReader.readWorkflowFromClasspath;

import io.serverlessworkflow.impl.WorkflowApplication;
import io.serverlessworkflow.impl.WorkflowDefinition;
import io.serverlessworkflow.impl.WorkflowInstance;
import io.serverlessworkflow.impl.persistence.PersistenceApplicationBuilder;
import io.serverlessworkflow.impl.persistence.PersistenceInstanceHandlers;
import io.serverlessworkflow.impl.persistence.bigmap.BytesMapPersistenceInstanceHandlers;
import io.serverlessworkflow.impl.persistence.mvstore.MVStorePersistenceStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class DBGenerator {

  public static void main(String[] args) throws IOException {
    runInstance("db-samples/running.db", false);
    runInstance("db-samples/suspended.db", true);
  }

  private static void runInstance(String dbName, boolean suspend) throws IOException {
    Files.deleteIfExists(Path.of(dbName));
    try (PersistenceInstanceHandlers factories =
            BytesMapPersistenceInstanceHandlers.builder(new MVStorePersistenceStore(dbName))
                .build();
        WorkflowApplication application =
            PersistenceApplicationBuilder.builder(
                    WorkflowApplication.builder().withListener(new TraceExecutionListener()),
                    factories.writer())
                .build()) {
      WorkflowDefinition definition =
          application.workflowDefinition(
              readWorkflowFromClasspath("workflows-samples/set-listen-to-any.yaml"));
      WorkflowInstance instance = definition.instance(Map.of());
      instance.start();
      if (suspend) {
        instance.suspend();
      }
    }
  }
}
