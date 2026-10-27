/*
 * Copyright 2020-Present The Open Workflow Specification Authors
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
package org.openworkflow.sdk.impl.http;

import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.OptionalLong;
import org.openworkflow.sdk.impl.WorkflowApplication;
import org.openworkflow.sdk.impl.WorkflowModel;

public class DefaultHttpResponse implements WorkflowHttpResponse {

  private final WorkflowApplication application;
  private final HttpResponse<byte[]> response;

  public DefaultHttpResponse(WorkflowApplication application, HttpResponse<byte[]> response) {
    this.application = application;
    this.response = response;
  }

  @Override
  public byte[] asByteArray() {
    return response.body();
  }

  @Override
  public Object getHeader(String name) {
    return response.headers().firstValue(name).orElse(null);
  }

  @Override
  public int status() {
    return response.statusCode();
  }

  @Override
  public OptionalLong lastModified() {
    return response
        .headers()
        .firstValue("Last-Modified")
        .map(DefaultHttpResponse::fromGMTString)
        .map(OptionalLong::of)
        .orElseGet(OptionalLong::empty);
  }

  @Override
  public String errorDetails() {
    byte[] body = response.body();
    return body != null ? new String(response.body()) : "No error information";
  }

  @Override
  public Optional<WorkflowModel> asWorkflowModel() {
    return application.modelFactory().fromJson(response.body());
  }

  @Override
  public String toString() {
    return "DefaultHttpResponse [response=" + response + "]";
  }

  private static long fromGMTString(String str) {
    return ZonedDateTime.parse(str, DateTimeFormatter.RFC_1123_DATE_TIME)
        .toInstant()
        .toEpochMilli();
  }
}
