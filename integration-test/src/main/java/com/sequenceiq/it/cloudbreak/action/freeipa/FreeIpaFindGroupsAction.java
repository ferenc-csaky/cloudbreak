package com.sequenceiq.it.cloudbreak.action.freeipa;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sequenceiq.freeipa.api.v1.freeipa.test.model.CheckGroupsV1Request;
import com.sequenceiq.it.cloudbreak.FreeIpaClient;
import com.sequenceiq.it.cloudbreak.action.Action;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.freeipa.FreeIpaTestDto;
import com.sequenceiq.it.cloudbreak.exception.TestFailException;

public class FreeIpaFindGroupsAction implements Action<FreeIpaTestDto, FreeIpaClient> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FreeIpaFindGroupsAction.class);

    private final Set<String> groups;

    public FreeIpaFindGroupsAction(Set<String> groups) {
        this.groups = groups;
    }

    public FreeIpaTestDto action(TestContext testContext, FreeIpaTestDto testDto, FreeIpaClient client) throws Exception {
        CheckGroupsV1Request checkGroupsRequest = new CheckGroupsV1Request();
        checkGroupsRequest.setEnvironmentCrn(testDto.getResponse().getEnvironmentCrn());
        checkGroupsRequest.setGroups(groups);
        if (!client.getDefaultClient().getClientTestV1Endpoint().checkGroups(checkGroupsRequest).getResult()) {
            throw new TestFailException("Given freeipa groups cannot be found, please check FMS logs for details");
        }
        return testDto;
    }
}
