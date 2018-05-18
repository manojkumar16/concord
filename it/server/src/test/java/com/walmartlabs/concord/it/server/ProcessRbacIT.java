package com.walmartlabs.concord.it.server;

/*-
 * *****
 * Concord
 * -----
 * Copyright (C) 2017 Wal-Mart Store, Inc.
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

import com.walmartlabs.concord.server.api.org.OrganizationEntry;
import com.walmartlabs.concord.server.api.org.OrganizationResource;
import com.walmartlabs.concord.server.api.org.ResourceAccessEntry;
import com.walmartlabs.concord.server.api.org.ResourceAccessLevel;
import com.walmartlabs.concord.server.api.org.project.ProjectEntry;
import com.walmartlabs.concord.server.api.org.project.ProjectResource;
import com.walmartlabs.concord.server.api.org.project.ProjectVisibility;
import com.walmartlabs.concord.server.api.org.team.*;
import com.walmartlabs.concord.server.api.process.ProcessResource;
import com.walmartlabs.concord.server.api.process.ProcessStatus;
import com.walmartlabs.concord.server.api.process.StartProcessResponse;
import com.walmartlabs.concord.server.api.security.apikey.ApiKeyResource;
import com.walmartlabs.concord.server.api.security.apikey.CreateApiKeyRequest;
import com.walmartlabs.concord.server.api.security.apikey.CreateApiKeyResponse;
import com.walmartlabs.concord.server.api.user.CreateUserRequest;
import com.walmartlabs.concord.server.api.user.UserResource;
import com.walmartlabs.concord.server.api.user.UserType;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.walmartlabs.concord.it.common.ITUtils.archive;
import static com.walmartlabs.concord.it.common.ServerClient.waitForStatus;
import static org.junit.Assert.fail;

public class ProcessRbacIT extends AbstractServerIT {

    @Test(timeout = 60000)
    public void test() throws Exception {
        // create a new org

        String orgName = "org_" + randomString();

        OrganizationResource orgResource = proxy(OrganizationResource.class);
        orgResource.createOrUpdate(new OrganizationEntry(orgName));

        // add the user A

        UserResource userResource = proxy(UserResource.class);

        String userAName = "userA_" + randomString();
        userResource.createOrUpdate(new CreateUserRequest(userAName, UserType.LOCAL));

        ApiKeyResource apiKeyResource = proxy(ApiKeyResource.class);
        CreateApiKeyResponse apiKeyA = apiKeyResource.create(new CreateApiKeyRequest(userAName));

        // create the user A's team

        String teamName = "team_" + randomString();

        TeamResource teamResource = proxy(TeamResource.class);
        CreateTeamResponse ctr = teamResource.createOrUpdate(orgName, new TeamEntry(teamName));

        teamResource.addUsers(orgName, teamName, false, Collections.singleton(new TeamUserEntry(userAName, TeamRole.MEMBER)));

        // switch to the user A and create a new private project

        setApiKey(apiKeyA.getKey());

        String projectName = "project_" + randomString();

        ProjectResource projectResource = proxy(ProjectResource.class);
        projectResource.createOrUpdate(orgName, new ProjectEntry(projectName, ProjectVisibility.PRIVATE));

        // grant the team access to the project

        projectResource.updateAccessLevel(orgName, projectName, new ResourceAccessEntry(ctr.getId(), orgName, teamName, ResourceAccessLevel.READER));

        // start a new process using the project as the user A

        byte[] payload = archive(ProcessRbacIT.class.getResource("processRbac").toURI());
        Map<String, Object> input = new HashMap<>();
        input.put("archive", payload);
        input.put("org", orgName);
        input.put("project", projectName);

        StartProcessResponse spr = start(input);

        ProcessResource processResource = proxy(ProcessResource.class);
        waitForStatus(processResource, spr.getInstanceId(), ProcessStatus.FINISHED);

        // switch to admin and add the user B

        resetApiKey();

        String userBName = "userB_" + randomString();
        userResource.createOrUpdate(new CreateUserRequest(userBName, UserType.LOCAL));

        CreateApiKeyResponse apiKeyB = apiKeyResource.create(new CreateApiKeyRequest(userBName));

        // switch to the user B and try starting a process using the project

        setApiKey(apiKeyB.getKey());

        try {
            start(input);
            fail("Should fail");
        } catch (WebApplicationException e) {
        }

        // switch to admin and add the user B to the team

        resetApiKey();

        teamResource.addUsers(orgName, teamName, false, Collections.singleton(new TeamUserEntry(userBName, TeamRole.MEMBER)));

        // switch to the user B and start a process using the project

        setApiKey(apiKeyB.getKey());

        spr = start(input);
        waitForStatus(processResource, spr.getInstanceId(), ProcessStatus.FINISHED);
    }
}
