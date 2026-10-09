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
package org.openworkflow.fluent.spec.spi;

import org.openworkflow.fluent.spec.CallGrpcTaskBuilder;
import org.openworkflow.fluent.spec.CallHttpTaskBuilder;
import org.openworkflow.fluent.spec.CallOpenAPITaskBuilder;
import org.openworkflow.fluent.spec.EmitTaskBuilder;
import org.openworkflow.fluent.spec.ForEachTaskBuilder;
import org.openworkflow.fluent.spec.ForkTaskBuilder;
import org.openworkflow.fluent.spec.ListenTaskBuilder;
import org.openworkflow.fluent.spec.RaiseTaskBuilder;
import org.openworkflow.fluent.spec.SetTaskBuilder;
import org.openworkflow.fluent.spec.SwitchTaskBuilder;
import org.openworkflow.fluent.spec.TaskItemListBuilder;
import org.openworkflow.fluent.spec.TryTaskBuilder;
import org.openworkflow.fluent.spec.WaitTaskBuilder;
import org.openworkflow.fluent.spec.WorkflowTaskBuilder;

/**
 * Documents the exposed fluent `do` DSL.
 *
 * @see <a
 *     href="https://github.com/serverlessworkflow/specification/blob/main/dsl-reference.md#do">CNCF
 *     DSL Reference - Do</a>
 */
public interface DoFluent<T>
    extends SetFluent<SetTaskBuilder, T>,
        SwitchFluent<SwitchTaskBuilder, T>,
        TryCatchFluent<TryTaskBuilder<TaskItemListBuilder>, T>,
        CallHttpFluent<CallHttpTaskBuilder, T>,
        EmitFluent<EmitTaskBuilder, T>,
        ForEachFluent<ForEachTaskBuilder<TaskItemListBuilder>, T>,
        ForkFluent<ForkTaskBuilder, T>,
        ListenFluent<ListenTaskBuilder, T>,
        WaitFluent<WaitTaskBuilder, T>,
        RaiseFluent<RaiseTaskBuilder, T>,
        CallOpenAPIFluent<CallOpenAPITaskBuilder, T>,
        CallGrpcFluent<CallGrpcTaskBuilder, T>,
        WorkflowFluent<WorkflowTaskBuilder, T> {}
