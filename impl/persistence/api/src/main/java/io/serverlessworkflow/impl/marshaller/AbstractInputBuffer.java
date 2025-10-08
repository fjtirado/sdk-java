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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractInputBuffer implements WorkflowInputBuffer {

  private final Collection<CustomObjectMarshaller> customMarshallers;

  protected AbstractInputBuffer(Collection<CustomObjectMarshaller> customMarshallers) {
    this.customMarshallers = customMarshallers;
  }

  @Override
  public <T extends Enum<T>> T readEnum(Class<T> enumClass) {
    return Enum.valueOf(enumClass, readString());
  }

  @Override
  public Instant readInstant() {
    return Instant.ofEpochMilli(readLong());
  }

  @Override
  public Map<String, Object> readMap() {
    int size = readInt();
    Map<String, Object> map = new LinkedHashMap<String, Object>(size);
    while (size-- > 0) {
      map.put(readString(), readObject());
    }
    return map;
  }

  @Override
  public Collection<Object> readCollection() {
    int size = readInt();
    Collection<Object> col = new ArrayList<>(size);
    while (size-- > 0) {
      col.add(readObject());
    }
    return col;
  }

  protected Type readType() {
    return Type.values()[readByte()];
  }

  @Override
  public Object readObject() {

    Type type = readType();

    switch (type) {
      case NULL:
        return null;

      case SHORT:
        return readShort();

      case LONG:
        return readLong();

      case INT:
        return readInt();

      case BYTE:
        return readByte();

      case BYTES:
        return readBytes();

      case FLOAT:
        return readFloat();

      case DOUBLE:
        return readDouble();

      case BOOLEAN:
        return readBoolean();

      case STRING:
        return readString();

      case MAP:
        return readMap();

      case COLLECTION:
        return readCollection();

      case INSTANT:
        return readInstant();

      case CUSTOM:
        return readCustomObject();

      default:
        throw new IllegalStateException("Unsupported type " + type);
    }
  }

  protected Class<?> readClass() {
    String className = readString();
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    }
  }

  protected Class<?> loadClass(String className) throws ClassNotFoundException {
    return Class.forName(className);
  }

  protected Object readCustomObject() {
    Class<?> objectClass = readClass();
    return customMarshallers.stream()
        .filter(m -> m.getObjectClass().isAssignableFrom(objectClass))
        .findFirst()
        .map(m -> m.read(this))
        .orElseThrow(() -> new IllegalArgumentException("Unsupported type " + objectClass));
  }
}
