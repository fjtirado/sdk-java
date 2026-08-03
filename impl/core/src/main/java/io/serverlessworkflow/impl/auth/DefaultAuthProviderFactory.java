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
package io.serverlessworkflow.impl.auth;

import io.serverlessworkflow.api.types.AuthenticationPolicyUnion;
import io.serverlessworkflow.api.types.BasicAuthenticationPolicy;
import io.serverlessworkflow.api.types.BearerAuthenticationPolicy;
import io.serverlessworkflow.api.types.DigestAuthenticationPolicy;
import io.serverlessworkflow.api.types.EndpointConfiguration;
import io.serverlessworkflow.api.types.ReferenceableAuthenticationPolicy;
import io.serverlessworkflow.api.types.Workflow;
import io.serverlessworkflow.impl.WorkflowApplication;
import io.serverlessworkflow.impl.WorkflowDefinition;
import java.util.Optional;

public class DefaultAuthProviderFactory implements AuthProviderFactory {

  private static class DefaultAuthProviderFactoryHolder {
    private static final DefaultAuthProviderFactory instance = new DefaultAuthProviderFactory();
  }

  public static DefaultAuthProviderFactory factory() {
    return DefaultAuthProviderFactoryHolder.instance;
  }

  @Override
  public Optional<AuthProvider> getAuth(
      WorkflowDefinition definition, EndpointConfiguration configuration) {
    return configuration == null
        ? Optional.empty()
        : getAuth(definition, configuration.getAuthentication(), "GET");
  }

  @Override
  public Optional<AuthProvider> getAuth(
      WorkflowDefinition definition, ReferenceableAuthenticationPolicy auth, String method) {
    return OAuthUtils.resolvePolicy(definition.workflow(), auth)
        .flatMap(
            policy ->
                buildFromPolicy(definition.application(), definition.workflow(), policy, method));
  }

  private Optional<AuthProvider> buildFromPolicy(
      WorkflowApplication app,
      Workflow workflow,
      AuthenticationPolicyUnion authenticationPolicy,
      String method) {
    if (authenticationPolicy.getBasicAuthenticationPolicy() != null) {
      return Optional.ofNullable(
          basicAuthProvider(app, workflow, authenticationPolicy.getBasicAuthenticationPolicy()));
    } else if (authenticationPolicy.getBearerAuthenticationPolicy() != null) {
      return Optional.ofNullable(
          bearerAuthProvider(app, workflow, authenticationPolicy.getBearerAuthenticationPolicy()));
    } else if (authenticationPolicy.getDigestAuthenticationPolicy() != null) {
      return Optional.ofNullable(
          digestAuthProvider(
              app, workflow, authenticationPolicy.getDigestAuthenticationPolicy(), method));
    }
    return OAuthUtils.from(authenticationPolicy)
        .map(
            policyData ->
                policyData.scheme() == OAuthScheme.OPENID_CONNECT
                    ? openIdAuthProvider(app, workflow, policyData)
                    : oAuth2AuthProvider(app, workflow, policyData));
  }

  protected AuthProvider oAuth2AuthProvider(
      WorkflowApplication app, Workflow workflow, OAuthPolicyData policyData) {
    return new OAuth2AuthProvider(app, workflow, policyData);
  }

  protected AuthProvider openIdAuthProvider(
      WorkflowApplication app, Workflow workflow, OAuthPolicyData policyData) {
    return new OpenIdAuthProvider(app, workflow, policyData);
  }

  protected AuthProvider digestAuthProvider(
      WorkflowApplication app,
      Workflow workflow,
      DigestAuthenticationPolicy digestAuthenticationPolicy,
      String method) {

    return new DigestAuthProvider(app, workflow, digestAuthenticationPolicy, method);
  }

  protected AuthProvider bearerAuthProvider(
      WorkflowApplication app,
      Workflow workflow,
      BearerAuthenticationPolicy bearerAuthenticationPolicy) {
    return new BearerAuthProvider(app, workflow, bearerAuthenticationPolicy);
  }

  protected AuthProvider basicAuthProvider(
      WorkflowApplication app,
      Workflow workflow,
      BasicAuthenticationPolicy basicAuthenticationPolicy) {
    return new BasicAuthProvider(app, workflow, basicAuthenticationPolicy);
  }
}
