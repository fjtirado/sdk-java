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
package io.serverlessworkflow.impl.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class PersistenceUtils {

  private PersistenceUtils() {}

  public static <T> Map<String, List<T>> mapMapToMapList(Map<String, Map<Integer, T>> map) {
    return map.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, e -> PersistenceUtils.mapToList(e.getValue())));
  }

  public static <T> List<T> mapToList(Map<Integer, T> map) {
    List<T> list = new ArrayList<>(map.size());
    for (int i = 0; i < map.size(); i++) {
      list.add(map.get(i));
    }
    return list;
  }
}
