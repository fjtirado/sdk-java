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
package io.serverlessworkflow.impl.marshaller;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

public interface WorkflowOutputBuffer extends AutoCloseable {

  WorkflowOutputBuffer writeString(String text);

  WorkflowOutputBuffer writeInt(int number);

  WorkflowOutputBuffer writeShort(short number);

  WorkflowOutputBuffer writeLong(long number);

  WorkflowOutputBuffer writeFloat(float number);

  WorkflowOutputBuffer writeDouble(double number);

  WorkflowOutputBuffer writeBoolean(boolean bool);

  WorkflowOutputBuffer writeByte(byte one);

  WorkflowOutputBuffer writeBytes(byte[] bytes);

  WorkflowOutputBuffer writeInstant(Instant instant);

  WorkflowOutputBuffer writeMap(Map<String, Object> map);

  WorkflowOutputBuffer writeCollection(Collection<Object> col);

  WorkflowOutputBuffer writeObject(Object object);

  <T extends Enum<T>> WorkflowOutputBuffer writeEnum(T value);

  void close();
}
