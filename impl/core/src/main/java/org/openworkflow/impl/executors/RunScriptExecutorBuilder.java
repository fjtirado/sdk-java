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
package org.openworkflow.impl.executors;

import java.util.Objects;
import java.util.Optional;
import org.openworkflow.api.types.RunScript;
import org.openworkflow.api.types.RunTaskConfiguration;
import org.openworkflow.api.types.Script;
import org.openworkflow.api.types.ScriptUnion;
import org.openworkflow.impl.WorkflowApplication;
import org.openworkflow.impl.WorkflowDefinition;
import org.openworkflow.impl.WorkflowUtils;
import org.openworkflow.impl.resources.ResourceLoaderUtils;
import org.openworkflow.impl.scripts.ScriptLanguageId;
import org.openworkflow.impl.scripts.ScriptRunner;

public class RunScriptExecutorBuilder implements RunnableTaskBuilder<RunScript> {

  @Override
  public CallableTask build(RunScript taskConfiguration, WorkflowDefinition definition) {
    ScriptUnion scriptUnion = taskConfiguration.getScript();
    Script script = scriptUnion.get();
    ScriptLanguageId language = ScriptLanguageId.from(script.getLanguage());
    WorkflowApplication application = definition.application();

    return new RunScriptExecutor(
        script.getEnvironment() != null && script.getEnvironment().getAdditionalProperties() != null
            ? Optional.of(
                WorkflowUtils.buildMapResolver(
                    application, script.getEnvironment().getAdditionalProperties()))
            : Optional.empty(),
        script.getArguments() != null && script.getArguments().getAdditionalProperties() != null
            ? Optional.of(
                WorkflowUtils.buildMapResolver(
                    application, script.getArguments().getAdditionalProperties()))
            : Optional.empty(),
        scriptUnion.getInlineScript() != null
            ? WorkflowUtils.buildStringFilter(application, scriptUnion.getInlineScript().getCode())
            : (w, t, m) ->
                definition
                    .resourceLoader()
                    .load(
                        Objects.requireNonNull(
                                scriptUnion.getExternalScript(),
                                "External script is required if inline script was not set")
                            .getSource(),
                        ResourceLoaderUtils::readString,
                        w,
                        t,
                        m),
        taskConfiguration.isAwait(),
        taskConfiguration.getReturn(),
        application.serviceLoadedClasses(ScriptRunner.class).stream()
            .filter(s -> s.identifier().equals(language))
            .findFirst()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "No script runner implementation found for language " + language)));
  }

  @Override
  public boolean accept(Class<? extends RunTaskConfiguration> clazz) {
    return RunScript.class.equals(clazz);
  }
}
