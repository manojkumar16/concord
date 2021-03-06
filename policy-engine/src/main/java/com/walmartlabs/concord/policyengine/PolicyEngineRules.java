package com.walmartlabs.concord.policyengine;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 - 2018 Walmart Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import com.fasterxml.jackson.annotation.JsonProperty;

public class PolicyEngineRules {

    private final PolicyRules<DependencyRule> dependencyRules;
    private final PolicyRules<FileRule> fileRules;
    private final PolicyRules<TaskRule> taskRules;
    private final WorkspaceRule workspaceRule;
    private final ContainerRule containerRules;
    private final QueueRule queueRules;
    private final ProtectedTasksRule protectedTasksRules;
    private final PolicyRules<EntityRule> entityRules;

    public PolicyEngineRules(@JsonProperty("dependency") PolicyRules<DependencyRule> dependencyRules,
                             @JsonProperty("file") PolicyRules<FileRule> fileRules,
                             @JsonProperty("task") PolicyRules<TaskRule> taskRules,
                             @JsonProperty("workspace") WorkspaceRule workspaceRule,
                             @JsonProperty("container") ContainerRule containerRules,
                             @JsonProperty("queue") QueueRule queueRules,
                             @JsonProperty("protectedTask") ProtectedTasksRule protectedTasksRules,
                             @JsonProperty("entity") PolicyRules<EntityRule> entityRules) {

        this.dependencyRules = dependencyRules;
        this.fileRules = fileRules;
        this.taskRules = taskRules;
        this.workspaceRule = workspaceRule;
        this.containerRules = containerRules;
        this.queueRules = queueRules;
        this.protectedTasksRules = protectedTasksRules;
        this.entityRules = entityRules;
    }

    public PolicyRules<DependencyRule> getDependencyRules() {
        return dependencyRules;
    }

    public PolicyRules<FileRule> getFileRules() {
        return fileRules;
    }

    public PolicyRules<TaskRule> getTaskRules() {
        return taskRules;
    }

    public WorkspaceRule getWorkspaceRule() {
        return workspaceRule;
    }

    public ContainerRule getContainerRules() {
        return containerRules;
    }

    public QueueRule getQueueRules() {
        return queueRules;
    }

    public ProtectedTasksRule getProtectedTasksRules() {
        return protectedTasksRules;
    }

    public PolicyRules<EntityRule> getEntityRules() {
        return entityRules;
    }

    @Override
    public String toString() {
        return "PolicyEngineRules{" +
                "dependencyRules=" + dependencyRules +
                ", fileRules=" + fileRules +
                ", taskRules=" + taskRules +
                ", workspaceRule=" + workspaceRule +
                ", containerRules=" + containerRules +
                ", queueRules=" + queueRules +
                ", protectedTasksRules=" + protectedTasksRules +
                ", entityRules=" + entityRules +
                '}';
    }
}
