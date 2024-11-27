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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

public class QueueWorkflowPosition implements WorkflowPosition {

  private Deque<Object> queue;

  QueueWorkflowPosition() {
    this(new ArrayDeque<>());
  }

  private QueueWorkflowPosition(Deque<Object> list) {
    this.queue = list;
  }

  public QueueWorkflowPosition copy() {
    return new QueueWorkflowPosition(new ArrayDeque<>(this.queue));
  }

  @Override
  public WorkflowPosition addIndex(int index) {
    queue.add(index);
    return this;
  }

  @Override
  public WorkflowPosition addProperty(String prop) {
    queue.add(prop);
    return this;
  }

  @Override
  public String jsonPointer() {
    return queue.stream().map(Object::toString).collect(Collectors.joining("/"));
  }

  @Override
  public String toString() {
    return "ListWorkflowPosition [list=" + queue + "]";
  }

  @Override
  public WorkflowPosition back() {
    queue.removeLast();
    return this;
  }

  @Override
  public Object last() {
    return queue.pollLast();
  }
}
