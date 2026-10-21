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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.openworkflow.sdk.impl.TaskContext;
import org.openworkflow.sdk.impl.WorkflowApplication;
import org.openworkflow.sdk.impl.WorkflowContext;
import org.openworkflow.sdk.impl.WorkflowModel;

public class DefaultHttpRequest implements WorkflowHttpRequest {

  private final WorkflowHttpRequestInfo httpInfo;
  private final HttpClient client;
  private final WorkflowApplication app;

  public DefaultHttpRequest(
      WorkflowApplication app, HttpClient client, WorkflowHttpRequestInfo httpInfo) {
    this.httpInfo = httpInfo;
    this.client = client;
    this.app = app;
  }

  @Override
  public CompletableFuture<WorkflowHttpResponse> invoke(
      Optional<WorkflowContext> workflowContext, Optional<TaskContext> taskContext) {
    Builder builder = HttpRequest.newBuilder();
    WorkflowModel body = httpInfo.body();
    builder.method(
        httpInfo.method(),
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofByteArray(
                body.asJson()
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Underlying model needs to support json in order to use http"))));
    Map<String, Object> headers = httpInfo.headers();
    if (headers != null) {
      headers.forEach((k, v) -> builder.header(k, v != null ? v.toString() : null));
    }
    URI uri = httpInfo.uri();
    Map<String, Object> queryParams = httpInfo.queryParams();
    if (queryParams != null) {
      StringBuilder query = new StringBuilder();
      for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
        query.append(entry.getKey() + "=" + entry.getValue());
      }
      try {
        uri =
            new URI(
                uri.getScheme(),
                uri.getUserInfo(),
                uri.getHost(),
                uri.getPort(),
                uri.getPath(),
                uri.getQuery() == null ? query.toString() : uri.getQuery().concat(query.toString()),
                uri.getFragment());
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException(
            "Malformed uri after adding query params. Original URI: "
                + uri
                + " Query params: "
                + queryParams);
      }
    }
    builder.uri(uri);
    Long ifModifiedSince = httpInfo.ifModifiedSince();
    if (ifModifiedSince != null) {
      builder.header(
          WorkflowHTTPRequestHelper.IF_MODIFIED_SINCE_HEADER, ifModifiedSince.toString());
    }
    return WorkflowHTTPRequestHelper.processAuth(
            httpInfo, (k, v) -> builder.header(k, v), workflowContext, taskContext)
        .thenCompose(
            __ -> client.sendAsync(builder.build(), HttpResponse.BodyHandlers.ofByteArray()))
        .exceptionallyCompose(
            ex ->
                CompletableFuture.failedFuture(
                    WorkflowHTTPRequestHelper.fromError(ex, taskContext)))
        .thenApply(response -> new DefaultHttpResponse(app, response));
  }
}
