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
package io.serverlessworkflow.impl.persistence.mvstore;

import io.serverlessworkflow.api.types.Document;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.impl.WorkflowDefinitionData;
import io.serverlessworkflow.impl.marshaller.WorkflowBufferFactory;
import io.serverlessworkflow.impl.persistence.bigmap.BytesMapInstanceTransaction;
import io.serverlessworkflow.impl.persistence.hashing.HashFactory;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import org.h2.mvstore.MVStore;
import org.h2.mvstore.tx.Transaction;
import org.h2.mvstore.tx.TransactionMap;
import org.h2.mvstore.tx.TransactionStore;

public class MVStoreTransaction extends BytesMapInstanceTransaction {

  protected static final String ID_SEPARATOR = "-";
  private static final String PROCESSED_PREFIX = "PROCESSED" + ID_SEPARATOR;

  private final TransactionStore transactionStore;
  private final Deque<Transaction> transactionStack = new ArrayDeque<>();
  private final MVStore store;

  public MVStoreTransaction(
      MVStore store,
      TransactionStore transactionStore,
      WorkflowBufferFactory bufferFactory,
      HashFactory hashFactory,
      Lock hashLock) {
    super(bufferFactory, hashFactory, hashLock);
    this.transactionStore = transactionStore;
    transactionStack.add(transactionStore.begin());
    this.store = store;
  }

  protected static String identifier(Workflow workflow, String sep) {
    Document document = workflow.getDocument();
    return document.getNamespace() + sep + document.getName() + sep + document.getVersion();
  }

  @Override
  public Map<String, byte[]> instanceData(WorkflowDefinitionData workflowContext) {
    return openMap(workflowContext, "instances");
  }

  @Override
  public Map<String, byte[]> tasks(String instanceId) {
    return taskMap(instanceId);
  }

  @Override
  public Map<String, byte[]> status(WorkflowDefinitionData workflowContext) {
    return openMap(workflowContext, "status");
  }

  private Transaction currentTransaction() {
    return transactionStack.peekLast();
  }

  @Override
  public void removeTasks(String instanceId) {
    currentTransaction().removeMap(taskMap(instanceId));
  }

  private TransactionMap<String, byte[]> taskMap(String instanceId) {
    return currentTransaction().openMap(mapTaskName(instanceId));
  }

  private Map<String, byte[]> openMap(WorkflowDefinitionData workflowDefinition, String suffix) {
    return currentTransaction()
        .openMap(identifier(workflowDefinition.workflow(), ID_SEPARATOR) + ID_SEPARATOR + suffix);
  }

  private String mapTaskName(String instanceId) {
    return instanceId + ID_SEPARATOR + "tasks";
  }

  @Override
  public void commit(WorkflowDefinitionData definition) {
    currentTransaction().commit();
  }

  @Override
  public void rollback(WorkflowDefinitionData definition) {
    currentTransaction().rollback();
  }

  @Override
  protected Map<String, byte[]> applicationData() {
    return currentTransaction().openMap("APPLICATION");
  }

  @Override
  protected Map<String, byte[]> cloudEvents(String regId) {
    return currentTransaction().openMap("CLOUDEVENTS" + ID_SEPARATOR + regId);
  }

  @Override
  protected Map<String, byte[]> processedCloudEvents(String regId) {
    return currentTransaction().openMap(PROCESSED_PREFIX + regId);
  }

  @Override
  protected void deleteAllProcessedMaps() {
    store.getMapNames().stream()
        .filter(s -> s.startsWith(PROCESSED_PREFIX))
        .forEach(s -> currentTransaction().removeMap(currentTransaction().openMap(s)));
  }

  @Override
  protected Map<String, byte[]> blobData(String instanceId) {
    return currentTransaction().openMap(instanceId + ID_SEPARATOR + "blobs");
  }

  @Override
  protected void removeBlobData(String instanceId) {
    currentTransaction().removeMap((TransactionMap<?, ?>) blobData(instanceId));
  }

  @Override
  protected <T> T executeNewTransaction(Callable<T> runnable) {
    transactionStack.add(transactionStore.begin());
    try {
      T result = runnable.call();
      commit(null);
      return result;
    } catch (Exception ex) {
      rollback(null);
      throw new IllegalStateException(ex);
    } finally {
      transactionStack.pollLast();
    }
  }
}
