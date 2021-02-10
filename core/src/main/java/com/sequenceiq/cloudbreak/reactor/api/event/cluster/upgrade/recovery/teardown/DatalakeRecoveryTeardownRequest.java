package com.sequenceiq.cloudbreak.reactor.api.event.cluster.upgrade.recovery.teardown;

import com.sequenceiq.cloudbreak.reactor.api.event.StackEvent;

public class DatalakeRecoveryTeardownRequest extends StackEvent {

    public DatalakeRecoveryTeardownRequest(Long stackId) {
        super(stackId);
    }
}
